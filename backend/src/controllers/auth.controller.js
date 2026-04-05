const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { User, RefreshToken } = require('../models');
const { AppError } = require('../middleware/errorHandler');

// Generate access token
const generateAccessToken = (userId, email) => {
  return jwt.sign(
    { id: userId, email },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '1h' }
  );
};

// Generate refresh token
const generateRefreshToken = (userId, email) => {
  return jwt.sign(
    { id: userId, email },
    process.env.JWT_REFRESH_SECRET,
    { expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d' }
  );
};

// Store refresh token in database
const storeRefreshToken = async (userId, token) => {
  const expiresIn = process.env.JWT_REFRESH_EXPIRES_IN || '7d';
  const days = parseInt(expiresIn);
  const expiresAt = new Date(Date.now() + days * 24 * 60 * 60 * 1000);
  
  await RefreshToken.create({
    user_id: userId,
    token,
    expires_at: expiresAt
  });
};

// Register new user
exports.register = async (req, res, next) => {
  try {
    const { email, password, displayName } = req.body;

    // Validate input
    if (!email || !password || !displayName) {
      return next(new AppError('Email, password, and display name are required', 400));
    }

    // Check if user already exists
    const existingUser = await User.findOne({ where: { email } });

    if (existingUser) {
      return next(new AppError('Email already registered', 409));
    }

    // Hash password
    const passwordHash = await bcrypt.hash(password, 10);

    // Create user
    const user = await User.create({
      email,
      password_hash: passwordHash,
      display_name: displayName
    });

    // Generate tokens
    const accessToken = generateAccessToken(user.id, email);
    const refreshToken = generateRefreshToken(user.id, email);

    // Store refresh token
    await storeRefreshToken(user.id, refreshToken);

    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: {
        user: {
          id: user.id,
          email: user.email,
          displayName: user.display_name
        },
        accessToken,
        refreshToken
      }
    });
  } catch (error) {
    next(error);
  }
};

// Login user
exports.login = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    // Validate input
    if (!email || !password) {
      return next(new AppError('Email and password are required', 400));
    }

    // Find user
    const user = await User.findOne({ where: { email } });

    if (!user) {
      return next(new AppError('Invalid email or password', 401));
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, user.password_hash);

    if (!isPasswordValid) {
      return next(new AppError('Invalid email or password', 401));
    }

    // Generate tokens
    const accessToken = generateAccessToken(user.id, user.email);
    const refreshToken = generateRefreshToken(user.id, user.email);

    // Store refresh token
    await storeRefreshToken(user.id, refreshToken);

    res.status(200).json({
      success: true,
      message: 'Login successful',
      data: {
        user: {
          id: user.id,
          email: user.email,
          displayName: user.display_name,
          avatarUrl: user.avatar_url
        },
        accessToken,
        refreshToken
      }
    });
  } catch (error) {
    next(error);
  }
};

// Refresh access token
exports.refreshToken = async (req, res, next) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return next(new AppError('Refresh token is required', 400));
    }

    // Verify refresh token
    let decoded;
    try {
      decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET);
    } catch (error) {
      return next(new AppError('Invalid or expired refresh token', 401));
    }

    // Check if refresh token exists in database
    const tokenRecord = await RefreshToken.findOne({ 
      where: { token: refreshToken },
      include: [{ model: User, as: 'user' }]
    });

    if (!tokenRecord) {
      return next(new AppError('Refresh token not found', 401));
    }

    // Check if token expired
    if (new Date(tokenRecord.expires_at) < new Date()) {
      await tokenRecord.destroy();
      return next(new AppError('Refresh token has expired', 401));
    }

    // Generate new access token
    const newAccessToken = generateAccessToken(tokenRecord.user_id, tokenRecord.user.email);

    res.status(200).json({
      success: true,
      message: 'Token refreshed successfully',
      data: {
        accessToken: newAccessToken
      }
    });
  } catch (error) {
    next(error);
  }
};

// Get current user info
exports.getMe = async (req, res, next) => {
  try {
    const userId = req.user.id;

    const user = await User.findByPk(userId, {
      attributes: ['id', 'email', 'display_name', 'avatar_url', 'created_at']
    });

    if (!user) {
      return next(new AppError('User not found', 404));
    }

    res.status(200).json({
      success: true,
      data: {
        user: {
          id: user.id,
          email: user.email,
          displayName: user.display_name,
          avatarUrl: user.avatar_url,
          createdAt: user.created_at
        }
      }
    });
  } catch (error) {
    next(error);
  }
};
