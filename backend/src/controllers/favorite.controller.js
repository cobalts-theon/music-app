const { Favorite, Song, Artist } = require('../models');
const { AppError } = require('../middleware/errorHandler');

// Get user favorites
exports.getFavorites = async (req, res, next) => {
  try {
    const userId = req.user.id;

    const favorites = await Favorite.findAll({
      where: { user_id: userId },
      include: [
        {
          model: Song,
          as: 'song',
          include: [
            {
              model: Artist,
              as: 'artist',
              attributes: ['id', 'name', 'avatar_url']
            }
          ]
        }
      ],
      order: [['created_at', 'DESC']]
    });

    const songs = favorites.map(fav => fav.song);

    res.status(200).json({
      success: true,
      data: songs
    });
  } catch (error) {
    next(error);
  }
};

// Add song to favorites
exports.addFavorite = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { songId } = req.body;

    if (!songId) {
      return next(new AppError('Song ID is required', 400));
    }

    // Check if song exists
    const song = await Song.findByPk(songId);

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    // Check if already favorited
    const existingFavorite = await Favorite.findOne({
      where: {
        user_id: userId,
        song_id: songId
      }
    });

    if (existingFavorite) {
      return next(new AppError('Song already in favorites', 409));
    }

    await Favorite.create({
      user_id: userId,
      song_id: songId
    });

    res.status(200).json({
      success: true,
      message: 'Song added to favorites'
    });
  } catch (error) {
    next(error);
  }
};

// Remove song from favorites
exports.removeFavorite = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { songId } = req.params;

    const favorite = await Favorite.findOne({
      where: {
        user_id: userId,
        song_id: songId
      }
    });

    if (!favorite) {
      return next(new AppError('Song not in favorites', 404));
    }

    await favorite.destroy();

    res.status(200).json({
      success: true,
      message: 'Song removed from favorites'
    });
  } catch (error) {
    next(error);
  }
};
