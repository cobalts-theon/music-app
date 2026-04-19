const authService = require('../services/auth.service');

// Register new user
exports.register = async (req, res, next) => {
  try {
    const data = await authService.registerWithEmail(req.body);

    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data
    });
  } catch (error) {
    next(error);
  }
};

// Login user
exports.login = async (req, res, next) => {
  try {
    const data = await authService.loginWithEmail(req.body);

    res.status(200).json({
      success: true,
      message: 'Login successful',
      data
    });
  } catch (error) {
    next(error);
  }
};

// Login/Register user with Google ID token
exports.googleAuth = async (req, res, next) => {
  try {
    const { authData, isNewUser } = await authService.loginOrRegisterWithGoogle(req.body);

    res.status(200).json({
      success: true,
      message: isNewUser ? 'User registered with Google successfully' : 'Google login successful',
      data: authData
    });
  } catch (error) {
    next(error);
  }
};

// Refresh access token
exports.refreshToken = async (req, res, next) => {
  try {
    const { refreshToken } = req.body;
    const data = await authService.refreshAccessToken(refreshToken);

    res.status(200).json({
      success: true,
      message: 'Token refreshed successfully',
      data
    });
  } catch (error) {
    next(error);
  }
};

// Get current user info
exports.getMe = async (req, res, next) => {
  try {
    const data = await authService.getMe(req.user.id);

    res.status(200).json({
      success: true,
      data
    });
  } catch (error) {
    next(error);
  }
};
