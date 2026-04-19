const jwt = require('jsonwebtoken');
const { AppError } = require('../middleware/errorHandler');

const DURATION_TO_MS = {
  s: 1000,
  m: 60 * 1000,
  h: 60 * 60 * 1000,
  d: 24 * 60 * 60 * 1000
};

const parseDurationToMs = (duration) => {
  const value = String(duration || '').trim();
  const shortFormat = value.match(/^(\d+)\s*([smhd])$/i);

  if (shortFormat) {
    const amount = parseInt(shortFormat[1], 10);
    const unit = shortFormat[2].toLowerCase();
    return amount * DURATION_TO_MS[unit];
  }

  if (/^\d+$/.test(value)) {
    return parseInt(value, 10) * DURATION_TO_MS.d;
  }

  return 7 * DURATION_TO_MS.d;
};

const getJwtSecret = () => {
  if (!process.env.JWT_SECRET) {
    throw new AppError('JWT secret is not configured', 500);
  }
  return process.env.JWT_SECRET;
};

const getJwtRefreshSecret = () => {
  if (!process.env.JWT_REFRESH_SECRET) {
    throw new AppError('JWT refresh secret is not configured', 500);
  }
  return process.env.JWT_REFRESH_SECRET;
};

const generateAccessToken = (userId, email) => jwt.sign(
  { id: userId, email },
  getJwtSecret(),
  { expiresIn: process.env.JWT_EXPIRES_IN || '1h' }
);

const generateRefreshToken = (userId, email) => jwt.sign(
  { id: userId, email },
  getJwtRefreshSecret(),
  { expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d' }
);

const getRefreshTokenExpiresAt = () => {
  const refreshExpiry = process.env.JWT_REFRESH_EXPIRES_IN || '7d';
  return new Date(Date.now() + parseDurationToMs(refreshExpiry));
};

const verifyRefreshToken = (token) => {
  try {
    return jwt.verify(token, getJwtRefreshSecret());
  } catch (error) {
    throw new AppError('Invalid or expired refresh token', 401);
  }
};

module.exports = {
  generateAccessToken,
  generateRefreshToken,
  getRefreshTokenExpiresAt,
  verifyRefreshToken
};
