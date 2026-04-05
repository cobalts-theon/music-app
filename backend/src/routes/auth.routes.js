const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const authController = require('../controllers/auth.controller');
const { protect } = require('../middleware/auth');
const validate = require('../middleware/validator');

// Register validation
const registerValidation = [
  body('email').isEmail().withMessage('Please provide a valid email'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),
  body('displayName').notEmpty().withMessage('Display name is required'),
  validate
];

// Login validation
const loginValidation = [
  body('email').isEmail().withMessage('Please provide a valid email'),
  body('password').notEmpty().withMessage('Password is required'),
  validate
];

// Refresh token validation
const refreshValidation = [
  body('refreshToken').notEmpty().withMessage('Refresh token is required'),
  validate
];

router.post('/register', registerValidation, authController.register);
router.post('/login', loginValidation, authController.login);
router.post('/refresh', refreshValidation, authController.refreshToken);
router.get('/me', protect, authController.getMe);

module.exports = router;
