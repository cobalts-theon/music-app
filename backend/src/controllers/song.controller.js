const { Song, Artist, Album } = require('../models');
const { AppError } = require('../middleware/errorHandler');
const { Op } = require('sequelize');

// Get all songs
exports.getAllSongs = async (req, res, next) => {
  try {
    const { genre, search, limit = 50, offset = 0 } = req.query;

    const where = {};
    
    if (genre) {
      where.genre = genre;
    }
    
    if (search) {
      where.title = { [Op.like]: `%${search}%` };
    }

    const songs = await Song.findAll({
      where,
      include: [
        { 
          model: Artist, 
          as: 'artist',
          attributes: ['id', 'name', 'avatar_url']
        },
        { 
          model: Album, 
          as: 'album',
          attributes: ['id', 'title', 'cover_url']
        }
      ],
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['created_at', 'DESC']]
    });

    res.status(200).json({
      success: true,
      data: songs
    });
  } catch (error) {
    next(error);
  }
};

// Get single song
exports.getSong = async (req, res, next) => {
  try {
    const { id } = req.params;

    const song = await Song.findByPk(id, {
      include: [
        { 
          model: Artist, 
          as: 'artist',
          attributes: ['id', 'name', 'bio', 'avatar_url']
        },
        { 
          model: Album, 
          as: 'album',
          attributes: ['id', 'title', 'cover_url', 'release_date']
        }
      ]
    });

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    // Increment play count
    await song.increment('play_count');

    res.status(200).json({
      success: true,
      data: song
    });
  } catch (error) {
    next(error);
  }
};

// Create new song
exports.createSong = async (req, res, next) => {
  try {
    const { title, artistId, albumId, audioUrl, duration, coverUrl, genre, lyrics } = req.body;

    if (!title || !artistId || !audioUrl || !duration) {
      return next(new AppError('Title, artist, audio URL, and duration are required', 400));
    }

    // Check if artist exists
    const artist = await Artist.findByPk(artistId);
    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    // Check if album exists (if provided)
    if (albumId) {
      const album = await Album.findByPk(albumId);
      if (!album) {
        return next(new AppError('Album not found', 404));
      }
    }

    const song = await Song.create({
      title,
      artist_id: artistId,
      album_id: albumId || null,
      audio_url: audioUrl,
      duration,
      cover_url: coverUrl || null,
      genre: genre || null,
      lyrics: lyrics || null
    });

    // Reload with associations
    await song.reload({
      include: [
        { model: Artist, as: 'artist' },
        { model: Album, as: 'album' }
      ]
    });

    res.status(201).json({
      success: true,
      message: 'Song created successfully',
      data: song
    });
  } catch (error) {
    next(error);
  }
};

// Update song
exports.updateSong = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { title, albumId, audioUrl, duration, coverUrl, genre, lyrics } = req.body;

    const song = await Song.findByPk(id);

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    // Update fields
    if (title) song.title = title;
    if (albumId !== undefined) song.album_id = albumId;
    if (audioUrl) song.audio_url = audioUrl;
    if (duration) song.duration = duration;
    if (coverUrl !== undefined) song.cover_url = coverUrl;
    if (genre !== undefined) song.genre = genre;
    if (lyrics !== undefined) song.lyrics = lyrics;

    await song.save();

    // Reload with associations
    await song.reload({
      include: [
        { model: Artist, as: 'artist' },
        { model: Album, as: 'album' }
      ]
    });

    res.status(200).json({
      success: true,
      message: 'Song updated successfully',
      data: song
    });
  } catch (error) {
    next(error);
  }
};

// Delete song
exports.deleteSong = async (req, res, next) => {
  try {
    const { id } = req.params;

    const song = await Song.findByPk(id);

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    await song.destroy();

    res.status(200).json({
      success: true,
      message: 'Song deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
