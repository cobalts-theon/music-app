const { Playlist, Song, Artist, PlaylistSong } = require('../models');
const { AppError } = require('../middleware/errorHandler');

// Get user playlists
exports.getUserPlaylists = async (req, res, next) => {
  try {
    const userId = req.user.id;

    const playlists = await Playlist.findAll({
      where: { user_id: userId },
      include: [
        {
          model: Song,
          as: 'songs',
          attributes: ['id', 'title', 'duration', 'cover_url'],
          include: [
            {
              model: Artist,
              as: 'artist',
              attributes: ['id', 'name']
            }
          ],
          through: { attributes: ['position'] }
        }
      ],
      order: [['created_at', 'DESC']]
    });

    res.status(200).json({
      success: true,
      data: playlists
    });
  } catch (error) {
    next(error);
  }
};

// Get single playlist
exports.getPlaylist = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;

    const playlist = await Playlist.findOne({
      where: { id },
      include: [
        {
          model: Song,
          as: 'songs',
          include: [
            {
              model: Artist,
              as: 'artist',
              attributes: ['id', 'name', 'avatar_url']
            }
          ],
          through: { attributes: ['position', 'added_at'] }
        }
      ]
    });

    if (!playlist) {
      return next(new AppError('Playlist not found', 404));
    }

    // Check if user has access (owner or public)
    if (playlist.user_id !== userId && !playlist.is_public) {
      return next(new AppError('Access denied', 403));
    }

    res.status(200).json({
      success: true,
      data: playlist
    });
  } catch (error) {
    next(error);
  }
};

// Create playlist
exports.createPlaylist = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { name, description, coverUrl, isPublic } = req.body;

    if (!name) {
      return next(new AppError('Playlist name is required', 400));
    }

    const playlist = await Playlist.create({
      user_id: userId,
      name,
      description: description || null,
      cover_url: coverUrl || null,
      is_public: isPublic || false
    });

    res.status(201).json({
      success: true,
      message: 'Playlist created successfully',
      data: playlist
    });
  } catch (error) {
    next(error);
  }
};

// Update playlist
exports.updatePlaylist = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;
    const { name, description, coverUrl, isPublic } = req.body;

    const playlist = await Playlist.findByPk(id);

    if (!playlist) {
      return next(new AppError('Playlist not found', 404));
    }

    if (playlist.user_id !== userId) {
      return next(new AppError('Access denied', 403));
    }

    if (name) playlist.name = name;
    if (description !== undefined) playlist.description = description;
    if (coverUrl !== undefined) playlist.cover_url = coverUrl;
    if (isPublic !== undefined) playlist.is_public = isPublic;

    await playlist.save();

    res.status(200).json({
      success: true,
      message: 'Playlist updated successfully',
      data: playlist
    });
  } catch (error) {
    next(error);
  }
};

// Delete playlist
exports.deletePlaylist = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;

    const playlist = await Playlist.findByPk(id);

    if (!playlist) {
      return next(new AppError('Playlist not found', 404));
    }

    if (playlist.user_id !== userId) {
      return next(new AppError('Access denied', 403));
    }

    await playlist.destroy();

    res.status(200).json({
      success: true,
      message: 'Playlist deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};

// Add song to playlist
exports.addSongToPlaylist = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;
    const { songId } = req.body;

    if (!songId) {
      return next(new AppError('Song ID is required', 400));
    }

    const playlist = await Playlist.findByPk(id);

    if (!playlist) {
      return next(new AppError('Playlist not found', 404));
    }

    if (playlist.user_id !== userId) {
      return next(new AppError('Access denied', 403));
    }

    const song = await Song.findByPk(songId);

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    // Check if song already in playlist
    const existingSong = await PlaylistSong.findOne({
      where: {
        playlist_id: id,
        song_id: songId
      }
    });

    if (existingSong) {
      return next(new AppError('Song already in playlist', 409));
    }

    // Get current max position
    const maxPosition = await PlaylistSong.max('position', {
      where: { playlist_id: id }
    });

    const position = (maxPosition || 0) + 1;

    await PlaylistSong.create({
      playlist_id: id,
      song_id: songId,
      position
    });

    res.status(200).json({
      success: true,
      message: 'Song added to playlist'
    });
  } catch (error) {
    next(error);
  }
};

// Remove song from playlist
exports.removeSongFromPlaylist = async (req, res, next) => {
  try {
    const { id, songId } = req.params;
    const userId = req.user.id;

    const playlist = await Playlist.findByPk(id);

    if (!playlist) {
      return next(new AppError('Playlist not found', 404));
    }

    if (playlist.user_id !== userId) {
      return next(new AppError('Access denied', 403));
    }

    const playlistSong = await PlaylistSong.findOne({
      where: {
        playlist_id: id,
        song_id: songId
      }
    });

    if (!playlistSong) {
      return next(new AppError('Song not in playlist', 404));
    }

    await playlistSong.destroy();

    res.status(200).json({
      success: true,
      message: 'Song removed from playlist'
    });
  } catch (error) {
    next(error);
  }
};
