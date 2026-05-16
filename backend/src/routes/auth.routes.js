const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const { protect } = require('../middleware/auth');
const {
  registerValidation,
  loginValidation,
  refreshValidation,
  googleValidation,
  forgotPasswordValidation,
  resetPasswordValidation
} = require('../validators/auth.validator');

router.post('/register', registerValidation, authController.register);
router.post('/login', loginValidation, authController.login);
router.post('/google', googleValidation, authController.googleAuth);
router.post('/forgot-password', forgotPasswordValidation, authController.requestPasswordResetOtp);
router.post('/reset-password', resetPasswordValidation, authController.resetPasswordWithOtp);
router.post('/refresh', refreshValidation, authController.refreshToken);
router.get('/me', protect, authController.getMe);

module.exports = router;
