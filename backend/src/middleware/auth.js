const jwt = require('jsonwebtoken');
const { AppError } = require('./errorHandler');

const protect = async (req, res, next) => {
  try {
    let token;

    // Get token from header
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }

    if (!token) {
      return next(new AppError('You are not logged in. Please log in to access this resource.', 401));
    }

    // Verify token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // Add user info to request
    req.user = {
      id: decoded.id,
      email: decoded.email,
      role: decoded.role || 'user'
    };

    next();
  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return next(new AppError('Invalid token. Please log in again.', 401));
    }
    if (error.name === 'TokenExpiredError') {
      return next(new AppError('Your token has expired. Please log in again.', 401));
    }
    next(error);
  }
};

const optionalAuth = async (req, res, next) => {
  try {
    let token;

    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }

    if (token) {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      req.user = {
        id: decoded.id,
        email: decoded.email,
        role: decoded.role || 'user'
      };
    }

    next();
  } catch (error) {
    // Continue without auth if token invalid
    next();
  }
};

const requireAdmin = async (req, res, next) => {
  try {
    if (!req.user?.id) {
      return next(new AppError('You are not logged in. Please log in to access this resource.', 401));
    }

    const { User } = require('../models');
    const user = await User.findByPk(req.user.id, {
      attributes: ['id', 'email', 'role']
    });

    if (!user || user.role !== 'admin') {
      return next(new AppError('Admin access required', 403));
    }

    req.user.role = user.role;
    next();
  } catch (error) {
    next(error);
  }
};

module.exports = { protect, optionalAuth, requireAdmin };
