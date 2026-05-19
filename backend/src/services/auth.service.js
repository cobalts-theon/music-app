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
const {
  sendPasswordResetOtpEmail,
  sendWelcomeEmail
} = require('./email.service');

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
  const accessToken = generateAccessToken(user.id, user.email, user.role || 'user');
  const refreshToken = generateRefreshToken(user.id, user.email, user.role || 'user');

  await storeRefreshToken(user.id, refreshToken);

  return {
    user: toAuthUser(user),
    accessToken,
    refreshToken
  };
};

const sendWelcomeNotification = async (user) => {
  try {
    await sendWelcomeEmail({
      to: user.email,
      displayName: user.display_name
    });
  } catch (error) {
    console.error(`[mail] Failed to send welcome email to ${user.email}:`, error.message);
  }
};

const registerWithEmail = async ({ email, password, displayName } = {}) => {
  if (!email) {
    throw new AppError('Email is required', 400);
  }

  if (!password) {
    throw new AppError('Password is required', 400);
  }

  if (!displayName) {
    throw new AppError('Display name is required', 400);
  }

  if (password.length < 6) {
    throw new AppError('Password must be at least 6 characters long', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();

  const existingUser = await User.findOne({ where: { email: normalizedEmail } });
  if (existingUser) {
    throw new AppError('This email is already registered', 409);
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const user = await User.create({
    email: normalizedEmail,
    password_hash: passwordHash,
    display_name: displayName.trim()
  });

  await sendWelcomeNotification(user);

  return issueAuthData(user);
};

const loginWithEmail = async ({ email, password } = {}) => {
  if (!email) {
    throw new AppError('Email is required', 400);
  }

  if (!password) {
    throw new AppError('Password is required', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();

  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) {
    throw new AppError('No account found with this email', 404);
  }

  const isPasswordValid = await bcrypt.compare(password, user.password_hash);
  if (!isPasswordValid) {
    throw new AppError('Incorrect password', 401);
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
    await sendWelcomeNotification(user);
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
    accessToken: generateAccessToken(
      tokenRecord.user_id,
      tokenRecord.user.email,
      tokenRecord.user.role || 'user'
    )
  };
};

const getMe = async (userId) => {
  const user = await User.findByPk(userId, {
    attributes: ['id', 'email', 'display_name', 'avatar_url', 'role', 'created_at']
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
  if (!user) {
    throw new AppError('No account found with this email', 404);
  }

  const otp = crypto.randomInt(100000, 1000000).toString();
  const otpHash = await bcrypt.hash(otp, 10);

  try {
    await sendPasswordResetOtpEmail({
      to: user.email,
      displayName: user.display_name,
      otp,
      expiresInMinutes: RESET_OTP_TTL_MS / 60_000
    });
  } catch (error) {
    console.error(`[mail] Failed to send password reset OTP to ${normalizedEmail}:`, error.message);
    if (error instanceof AppError) {
      throw error;
    }
    throw new AppError('Unable to send OTP email. Please try again later.', 502);
  }

  resetOtpStore.set(normalizedEmail, {
    otpHash,
    expiresAt: Date.now() + RESET_OTP_TTL_MS
  });

  return {
    email: normalizedEmail,
    otpExpiresInSeconds: RESET_OTP_TTL_MS / 1000
  };
};

const resetPasswordWithOtp = async ({ email, otp, newPassword } = {}) => {
  if (!email) {
    throw new AppError('Email is required', 400);
  }

  if (!otp) {
    throw new AppError('OTP is required', 400);
  }

  if (!newPassword) {
    throw new AppError('New password is required', 400);
  }

  if (newPassword.length < 6) {
    throw new AppError('New password must be at least 6 characters long', 400);
  }

  const normalizedEmail = email.toLowerCase().trim();
  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) {
    resetOtpStore.delete(normalizedEmail);
    throw new AppError('No account found with this email', 404);
  }

  const resetRecord = resetOtpStore.get(normalizedEmail);

  if (!resetRecord || resetRecord.expiresAt < Date.now()) {
    resetOtpStore.delete(normalizedEmail);
    throw new AppError('OTP is invalid or expired', 400);
  }

  const isOtpValid = await bcrypt.compare(otp.trim(), resetRecord.otpHash);
  if (!isOtpValid) {
    throw new AppError('OTP is invalid or expired', 400);
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
