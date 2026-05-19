const { UserLibrarySong, Song, Artist, Album } = require('../models');
const { AppError } = require('../middleware/errorHandler');

const songIncludes = [
  {
    model: Artist,
    as: 'artist',
    attributes: ['id', 'name', 'bio', 'avatar_url']
  },
  {
    model: Album,
    as: 'album',
    attributes: ['id', 'title', 'artist_id', 'cover_url', 'release_date']
  }
];

const parseSongId = (rawSongId) => {
  const songId = Number(rawSongId);
  return Number.isInteger(songId) && songId > 0 ? songId : null;
};

const findLibrarySong = (songId) => Song.findByPk(songId, {
  include: songIncludes
});

exports.getLibrarySongs = async (req, res, next) => {
  try {
    const userId = req.user.id;

    const libraryRows = await UserLibrarySong.findAll({
      where: { user_id: userId },
      include: [
        {
          model: Song,
          as: 'song',
          include: songIncludes
        }
      ],
      order: [['created_at', 'DESC']]
    });

    const songs = libraryRows
      .map((row) => row.song)
      .filter(Boolean);

    res.status(200).json({
      success: true,
      data: songs
    });
  } catch (error) {
    next(error);
  }
};

exports.addSongToLibrary = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const songId = parseSongId(req.body.songId);

    if (!songId) {
      return next(new AppError('Song ID is required', 400));
    }

    const song = await findLibrarySong(songId);
    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    const [, created] = await UserLibrarySong.findOrCreate({
      where: {
        user_id: userId,
        song_id: songId
      },
      defaults: {
        user_id: userId,
        song_id: songId
      }
    });

    res.status(created ? 201 : 200).json({
      success: true,
      message: created ? 'Song added to library' : 'Song already in library',
      data: song
    });
  } catch (error) {
    next(error);
  }
};

exports.removeSongFromLibrary = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const songId = parseSongId(req.params.songId);

    if (!songId) {
      return next(new AppError('Song ID is required', 400));
    }

    const deletedCount = await UserLibrarySong.destroy({
      where: {
        user_id: userId,
        song_id: songId
      }
    });

    res.status(200).json({
      success: true,
      message: deletedCount > 0 ? 'Song removed from library' : 'Song was not in library'
    });
  } catch (error) {
    next(error);
  }
};
