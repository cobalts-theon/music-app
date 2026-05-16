const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const { User, RefreshToken } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const {
  generateAccessToken,
  generateRefreshToken,
  getRefreshTokenExpiresAt,
  verifyRefreshToken
} = require('../utils/token');
const { toAuthUser, toProfileUser } = require('../utils/user');
const { verifyGoogleIdToken } = require('./googleAuth.service');

const RESET_OTP_TTL_MS = 10 * 60 * 1000;
const resetOtpStore = new Map();

const storeRefreshToken = async (userId, token) => {
  await RefreshToken.create({
    user_id: userId,
    token,
    expires_at: getRefreshTokenExpiresAt()
  });
};

const issueAuthData = async (user) => {
  const accessToken = generateAccessToken(user.id, user.email);
  const refreshToken = generateRefreshToken(user.id, user.email);

  await storeRefreshToken(user.id, refreshToken);

  return {
    user: toAuthUser(user),
    accessToken,
    refreshToken
  };
};

const registerWithEmail = async ({ email, password, displayName } = {}) => {
  if (!email || !password || !displayName) {
    throw new AppError('Email, password, and display name are required', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();

  const existingUser = await User.findOne({ where: { email: normalizedEmail } });
  if (existingUser) {
    throw new AppError('Email already registered', 409);
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const user = await User.create({
    email: normalizedEmail,
    password_hash: passwordHash,
    display_name: displayName.trim()
  });

  return issueAuthData(user);
};

const loginWithEmail = async ({ email, password } = {}) => {
  if (!email || !password) {
    throw new AppError('Email and password are required', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();

  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) {
    throw new AppError('Invalid email or password', 401);
  }

  const isPasswordValid = await bcrypt.compare(password, user.password_hash);
  if (!isPasswordValid) {
    throw new AppError('Invalid email or password', 401);
  }

  return issueAuthData(user);
};

const loginOrRegisterWithGoogle = async ({ idToken } = {}) => {
  if (!idToken) {
    throw new AppError('Google ID token is required', 400);
  }

  const googleProfile = await verifyGoogleIdToken(idToken);
  let user = await User.findOne({ where: { email: googleProfile.email } });
  let isNewUser = false;

  if (!user) {
    const randomPassword = crypto.randomBytes(32).toString('hex');
    const passwordHash = await bcrypt.hash(randomPassword, 10);

    user = await User.create({
      email: googleProfile.email,
      password_hash: passwordHash,
      display_name: googleProfile.displayName,
      avatar_url: googleProfile.avatarUrl
    });

    isNewUser = true;
  } else {
    let changed = false;

    if (googleProfile.displayName && user.display_name !== googleProfile.displayName) {
      user.display_name = googleProfile.displayName;
      changed = true;
    }

    if (googleProfile.avatarUrl && user.avatar_url !== googleProfile.avatarUrl) {
      user.avatar_url = googleProfile.avatarUrl;
      changed = true;
    }

    if (changed) {
      await user.save();
    }
  }

  return {
    authData: await issueAuthData(user),
    isNewUser
  };
};

const refreshAccessToken = async (refreshToken) => {
  if (!refreshToken) {
    throw new AppError('Refresh token is required', 400);
  }

  const decoded = verifyRefreshToken(refreshToken);
  const tokenRecord = await RefreshToken.findOne({
    where: { token: refreshToken },
    include: [{ model: User, as: 'user' }]
  });

  if (!tokenRecord || !tokenRecord.user) {
    throw new AppError('Refresh token not found', 401);
  }

  if (decoded.id !== tokenRecord.user_id) {
    throw new AppError('Invalid refresh token', 401);
  }

  if (new Date(tokenRecord.expires_at) < new Date()) {
    await tokenRecord.destroy();
    throw new AppError('Refresh token has expired', 401);
  }

  return {
    accessToken: generateAccessToken(tokenRecord.user_id, tokenRecord.user.email)
  };
};

const getMe = async (userId) => {
  const user = await User.findByPk(userId, {
    attributes: ['id', 'email', 'display_name', 'avatar_url', 'created_at']
  });

  if (!user) {
    throw new AppError('User not found', 404);
  }

  return {
    user: toProfileUser(user)
  };
};

const requestPasswordResetOtp = async ({ email } = {}) => {
  if (!email) {
    throw new AppError('Email is required', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();
  const user = await User.findOne({ where: { email: normalizedEmail } });
  const otp = crypto.randomInt(100000, 1000000).toString();

  if (user) {
    const otpHash = await bcrypt.hash(otp, 10);
    resetOtpStore.set(normalizedEmail, {
      otpHash,
      expiresAt: Date.now() + RESET_OTP_TTL_MS
    });

    console.log(`[auth] Password reset OTP for ${normalizedEmail}: ${otp}`);
  }

  const data = {
    email: normalizedEmail,
    otpExpiresInSeconds: RESET_OTP_TTL_MS / 1000
  };

  if (process.env.NODE_ENV !== 'production' && user) {
    data.devOtp = otp;
  }

  return data;
};

const resetPasswordWithOtp = async ({ email, otp, newPassword } = {}) => {
  if (!email || !otp || !newPassword) {
    throw new AppError('Email, OTP, and new password are required', 400);
  }

  if (newPassword.length < 6) {
    throw new AppError('Password must be at least 6 characters long', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();
  const resetRecord = resetOtpStore.get(normalizedEmail);

  if (!resetRecord || resetRecord.expiresAt < Date.now()) {
    resetOtpStore.delete(normalizedEmail);
    throw new AppError('OTP is invalid or expired', 400);
  }

  const isOtpValid = await bcrypt.compare(otp.trim(), resetRecord.otpHash);
  if (!isOtpValid) {
    throw new AppError('OTP is invalid or expired', 400);
  }

  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) {
    resetOtpStore.delete(normalizedEmail);
    throw new AppError('User not found', 404);
  }

  user.password_hash = await bcrypt.hash(newPassword, 10);
  await user.save();
  resetOtpStore.delete(normalizedEmail);

  return issueAuthData(user);
};

module.exports = {
  registerWithEmail,
  loginWithEmail,
  loginOrRegisterWithGoogle,
  refreshAccessToken,
  getMe,
  requestPasswordResetOtp,
  resetPasswordWithOtp
};
