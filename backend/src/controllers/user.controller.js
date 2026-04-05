const { User } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const bcrypt = require('bcryptjs');

// Get user profile
exports.getUser = async (req, res, next) => {
  try {
    const { id } = req.params;

    const user = await User.findByPk(id, {
      attributes: ['id', 'email', 'display_name', 'avatar_url', 'created_at']
    });

    if (!user) {
      return next(new AppError('User not found', 404));
    }

    res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    next(error);
  }
};

// Update user profile
exports.updateUser = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { displayName, avatarUrl, currentPassword, newPassword } = req.body;

    const user = await User.findByPk(userId);

    if (!user) {
      return next(new AppError('User not found', 404));
    }

    // Update display name
    if (displayName) {
      user.display_name = displayName;
    }

    // Update avatar
    if (avatarUrl !== undefined) {
      user.avatar_url = avatarUrl;
    }

    // Update password if provided
    if (newPassword) {
      if (!currentPassword) {
        return next(new AppError('Current password is required to change password', 400));
      }

      // Verify current password
      const isPasswordValid = await bcrypt.compare(currentPassword, user.password_hash);

      if (!isPasswordValid) {
        return next(new AppError('Current password is incorrect', 401));
      }

      // Hash new password
      user.password_hash = await bcrypt.hash(newPassword, 10);
    }

    await user.save();

    res.status(200).json({
      success: true,
      message: 'User updated successfully',
      data: {
        id: user.id,
        email: user.email,
        displayName: user.display_name,
        avatarUrl: user.avatar_url
      }
    });
  } catch (error) {
    next(error);
  }
};

// Delete user account
exports.deleteUser = async (req, res, next) => {
  try {
    const userId = req.user.id;

    const user = await User.findByPk(userId);

    if (!user) {
      return next(new AppError('User not found', 404));
    }

    await user.destroy();

    res.status(200).json({
      success: true,
      message: 'User account deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
