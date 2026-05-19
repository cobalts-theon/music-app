const bcrypt = require('bcryptjs');
const { User, Song, Artist, Album, Playlist } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const { toProfileUser } = require('../utils/user');

exports.getSummary = async (req, res, next) => {
  try {
    const [usersCount, songsCount, artistsCount, albumsCount, playlistsCount] = await Promise.all([
      User.count(),
      Song.count(),
      Artist.count(),
      Album.count(),
      Playlist.count()
    ]);

    res.status(200).json({
      success: true,
      data: {
        usersCount,
        songsCount,
        artistsCount,
        albumsCount,
        playlistsCount
      }
    });
  } catch (error) {
    next(error);
  }
};

exports.getUsers = async (req, res, next) => {
  try {
    const users = await User.findAll({
      attributes: ['id', 'email', 'display_name', 'avatar_url', 'role', 'created_at'],
      order: [['created_at', 'DESC']]
    });

    res.status(200).json({
      success: true,
      data: users
    });
  } catch (error) {
    next(error);
  }
};

exports.createUser = async (req, res, next) => {
  try {
    const { email, password, displayName, avatarUrl, role } = req.body;

    if (!email || !password || !displayName) {
      return next(new AppError('Email, password, and display name are required', 400));
    }

    if (password.length < 6) {
      return next(new AppError('Password must be at least 6 characters long', 400));
    }

    const normalizedEmail = email.toLowerCase().trim();
    const existingUser = await User.findOne({ where: { email: normalizedEmail } });
    if (existingUser) {
      return next(new AppError('This email is already registered', 409));
    }

    const user = await User.create({
      email: normalizedEmail,
      password_hash: await bcrypt.hash(password, 10),
      display_name: displayName.trim(),
      avatar_url: avatarUrl || null,
      role: role === 'admin' ? 'admin' : 'user'
    });

    res.status(201).json({
      success: true,
      message: 'User created successfully',
      data: toProfileUser(user)
    });
  } catch (error) {
    next(error);
  }
};

exports.updateUser = async (req, res, next) => {
  try {
    const userId = parseInt(req.params.id, 10);
    const { email, password, displayName, avatarUrl, role } = req.body;

    if (Number.isNaN(userId)) {
      return next(new AppError('Invalid user ID', 400));
    }

    const user = await User.findByPk(userId);
    if (!user) {
      return next(new AppError('User not found', 404));
    }

    if (email) {
      const normalizedEmail = email.toLowerCase().trim();
      const existingUser = await User.findOne({ where: { email: normalizedEmail } });
      if (existingUser && existingUser.id !== user.id) {
        return next(new AppError('This email is already registered', 409));
      }
      user.email = normalizedEmail;
    }

    if (displayName) {
      user.display_name = displayName.trim();
    }

    if (avatarUrl !== undefined) {
      user.avatar_url = avatarUrl || null;
    }

    if (role !== undefined) {
      if (!['user', 'admin'].includes(role)) {
        return next(new AppError('Invalid role', 400));
      }
      user.role = role;
    }

    if (password) {
      if (password.length < 6) {
        return next(new AppError('Password must be at least 6 characters long', 400));
      }
      user.password_hash = await bcrypt.hash(password, 10);
    }

    await user.save();

    res.status(200).json({
      success: true,
      message: 'User updated successfully',
      data: toProfileUser(user)
    });
  } catch (error) {
    next(error);
  }
};

exports.deleteUser = async (req, res, next) => {
  try {
    const userId = parseInt(req.params.id, 10);

    if (Number.isNaN(userId)) {
      return next(new AppError('Invalid user ID', 400));
    }

    if (userId === req.user.id) {
      return next(new AppError('You cannot delete your own admin account', 400));
    }

    const user = await User.findByPk(userId);
    if (!user) {
      return next(new AppError('User not found', 404));
    }

    await user.destroy();

    res.status(200).json({
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
