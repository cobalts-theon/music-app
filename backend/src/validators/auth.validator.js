const { body } = require('express-validator');
const validate = require('../middleware/validator');

const registerValidation = [
  body('email')
    .isEmail()
    .withMessage('Please provide a valid email')
    .normalizeEmail(),
  body('password')
    .isLength({ min: 6 })
    .withMessage('Password must be at least 6 characters long'),
  body('displayName')
    .trim()
    .notEmpty()
    .withMessage('Display name is required'),
  validate
];

const loginValidation = [
  body('email')
    .isEmail()
    .withMessage('Please provide a valid email')
    .normalizeEmail(),
  body('password')
    .notEmpty()
    .withMessage('Password is required'),
  validate
];

const refreshValidation = [
  body('refreshToken')
    .trim()
    .notEmpty()
    .withMessage('Refresh token is required'),
  validate
];

const googleValidation = [
  body('idToken')
    .trim()
    .notEmpty()
    .withMessage('Google ID token is required'),
  validate
];

module.exports = {
  registerValidation,
  loginValidation,
  refreshValidation,
  googleValidation
};
