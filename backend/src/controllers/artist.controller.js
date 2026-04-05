const { Artist, Album, Song } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const { Op } = require('sequelize');

// Get all artists
exports.getAllArtists = async (req, res, next) => {
  try {
    const { search, limit = 50, offset = 0 } = req.query;

    const where = {};
    
    if (search) {
      where.name = { [Op.like]: `%${search}%` };
    }

    const artists = await Artist.findAll({
      where,
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['name', 'ASC']]
    });

    res.status(200).json({
      success: true,
      data: artists
    });
  } catch (error) {
    next(error);
  }
};

// Get single artist with albums and songs
exports.getArtist = async (req, res, next) => {
  try {
    const { id } = req.params;

    const artist = await Artist.findByPk(id, {
      include: [
        { 
          model: Album, 
          as: 'albums',
          attributes: ['id', 'title', 'cover_url', 'release_date']
        },
        { 
          model: Song, 
          as: 'songs',
          attributes: ['id', 'title', 'duration', 'genre', 'play_count']
        }
      ]
    });

    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    res.status(200).json({
      success: true,
      data: artist
    });
  } catch (error) {
    next(error);
  }
};

// Create new artist
exports.createArtist = async (req, res, next) => {
  try {
    const { name, bio, avatarUrl } = req.body;

    if (!name) {
      return next(new AppError('Artist name is required', 400));
    }

    const artist = await Artist.create({
      name,
      bio: bio || null,
      avatar_url: avatarUrl || null
    });

    res.status(201).json({
      success: true,
      message: 'Artist created successfully',
      data: artist
    });
  } catch (error) {
    next(error);
  }
};

// Update artist
exports.updateArtist = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { name, bio, avatarUrl } = req.body;

    const artist = await Artist.findByPk(id);

    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    if (name) artist.name = name;
    if (bio !== undefined) artist.bio = bio;
    if (avatarUrl !== undefined) artist.avatar_url = avatarUrl;

    await artist.save();

    res.status(200).json({
      success: true,
      message: 'Artist updated successfully',
      data: artist
    });
  } catch (error) {
    next(error);
  }
};

// Delete artist
exports.deleteArtist = async (req, res, next) => {
  try {
    const { id } = req.params;

    const artist = await Artist.findByPk(id);

    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    await artist.destroy();

    res.status(200).json({
      success: true,
      message: 'Artist deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
