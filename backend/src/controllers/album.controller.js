const { Album, Artist, Song } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const { Op } = require('sequelize');

// Get all albums
exports.getAllAlbums = async (req, res, next) => {
  try {
    const { artistId, search, limit = 50, offset = 0 } = req.query;

    const where = {};
    
    if (artistId) {
      where.artist_id = artistId;
    }
    
    if (search) {
      where.title = { [Op.like]: `%${search}%` };
    }

    const albums = await Album.findAll({
      where,
      include: [
        { 
          model: Artist, 
          as: 'artist',
          attributes: ['id', 'name', 'avatar_url']
        }
      ],
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['release_date', 'DESC']]
    });

    res.status(200).json({
      success: true,
      data: albums
    });
  } catch (error) {
    next(error);
  }
};

// Get single album with songs
exports.getAlbum = async (req, res, next) => {
  try {
    const { id } = req.params;

    const album = await Album.findByPk(id, {
      include: [
        { 
          model: Artist, 
          as: 'artist',
          attributes: ['id', 'name', 'bio', 'avatar_url']
        },
        { 
          model: Song, 
          as: 'songs',
          attributes: ['id', 'title', 'duration', 'audio_url', 'genre', 'play_count']
        }
      ]
    });

    if (!album) {
      return next(new AppError('Album not found', 404));
    }

    res.status(200).json({
      success: true,
      data: album
    });
  } catch (error) {
    next(error);
  }
};

// Create new album
exports.createAlbum = async (req, res, next) => {
  try {
    const { title, artistId, coverUrl, releaseDate } = req.body;

    if (!title || !artistId) {
      return next(new AppError('Title and artist are required', 400));
    }

    // Check if artist exists
    const artist = await Artist.findByPk(artistId);
    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    const album = await Album.create({
      title,
      artist_id: artistId,
      cover_url: coverUrl || null,
      release_date: releaseDate || null
    });

    // Reload with artist
    await album.reload({
      include: [{ model: Artist, as: 'artist' }]
    });

    res.status(201).json({
      success: true,
      message: 'Album created successfully',
      data: album
    });
  } catch (error) {
    next(error);
  }
};

// Update album
exports.updateAlbum = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { title, coverUrl, releaseDate } = req.body;

    const album = await Album.findByPk(id);

    if (!album) {
      return next(new AppError('Album not found', 404));
    }

    if (title) album.title = title;
    if (coverUrl !== undefined) album.cover_url = coverUrl;
    if (releaseDate !== undefined) album.release_date = releaseDate;

    await album.save();

    // Reload with artist
    await album.reload({
      include: [{ model: Artist, as: 'artist' }]
    });

    res.status(200).json({
      success: true,
      message: 'Album updated successfully',
      data: album
    });
  } catch (error) {
    next(error);
  }
};

// Delete album
exports.deleteAlbum = async (req, res, next) => {
  try {
    const { id } = req.params;

    const album = await Album.findByPk(id);

    if (!album) {
      return next(new AppError('Album not found', 404));
    }

    await album.destroy();

    res.status(200).json({
      success: true,
      message: 'Album deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
