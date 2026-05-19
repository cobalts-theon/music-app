const { Song, Artist, Album, Playlist, User } = require('../models');
const { AppError } = require('../middleware/errorHandler');

const APP_NAME = "Cinder's Soul";
const DEFAULT_IMAGE_PATH = '/assets/CindersSoulslogo.png';

const getPublicBaseUrl = (req) => {
  const configuredBaseUrl = process.env.SHARE_BASE_URL || process.env.APP_PUBLIC_URL || '';
  if (configuredBaseUrl.trim()) {
    return configuredBaseUrl.trim().replace(/\/+$/, '');
  }

  return `${req.protocol}://${req.get('host')}`;
};

const escapeHtml = (value) => String(value ?? '')
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;');

const absoluteUrl = (req, url) => {
  const baseUrl = getPublicBaseUrl(req);
  if (!url || !String(url).trim()) {
    return `${baseUrl}${DEFAULT_IMAGE_PATH}`;
  }

  const rawUrl = String(url).trim();
  if (rawUrl.startsWith('/')) {
    return `${baseUrl}${rawUrl}`;
  }

  try {
    const parsedUrl = new URL(rawUrl);
    const localHosts = new Set(['localhost', '127.0.0.1', '10.0.2.2']);
    if (localHosts.has(parsedUrl.hostname)) {
      return `${baseUrl}${parsedUrl.pathname}${parsedUrl.search}`;
    }
    return rawUrl;
  } catch (_) {
    return `${baseUrl}/${rawUrl.replace(/^\/+/, '')}`;
  }
};

const renderSharePage = (req, res, options) => {
  const pageUrl = `${getPublicBaseUrl(req)}${req.originalUrl}`;
  const title = `${options.title} | ${APP_NAME}`;
  const description = options.description || `Listen on ${APP_NAME}`;
  const imageUrl = absoluteUrl(req, options.imageUrl);
  const type = options.type || 'website';

  res.type('html').send(`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${escapeHtml(title)}</title>
  <meta name="description" content="${escapeHtml(description)}">
  <meta property="og:site_name" content="${escapeHtml(APP_NAME)}">
  <meta property="og:type" content="${escapeHtml(type)}">
  <meta property="og:title" content="${escapeHtml(title)}">
  <meta property="og:description" content="${escapeHtml(description)}">
  <meta property="og:image" content="${escapeHtml(imageUrl)}">
  <meta property="og:url" content="${escapeHtml(pageUrl)}">
  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:title" content="${escapeHtml(title)}">
  <meta name="twitter:description" content="${escapeHtml(description)}">
  <meta name="twitter:image" content="${escapeHtml(imageUrl)}">
</head>
<body>
  <main>
    <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(options.title)}" width="320">
    <h1>${escapeHtml(options.title)}</h1>
    <p>${escapeHtml(description)}</p>
    <p><a href="${escapeHtml(pageUrl)}">Open ${escapeHtml(APP_NAME)}</a></p>
  </main>
</body>
</html>`);
};

exports.shareSong = async (req, res, next) => {
  try {
    const song = await Song.findByPk(req.params.id, {
      include: [
        { model: Artist, as: 'artist', attributes: ['id', 'name', 'avatar_url'] },
        { model: Album, as: 'album', attributes: ['id', 'title', 'cover_url'] }
      ]
    });

    if (!song) {
      return next(new AppError('Song not found', 404));
    }

    const artistName = song.artist?.name || 'Unknown artist';
    const albumTitle = song.album?.title || 'Single';
    renderSharePage(req, res, {
      title: song.title,
      description: `${artistName} - ${albumTitle}`,
      imageUrl: song.cover_url || song.album?.cover_url || song.artist?.avatar_url,
      type: 'music.song'
    });
  } catch (error) {
    next(error);
  }
};

exports.shareAlbum = async (req, res, next) => {
  try {
    const album = await Album.findByPk(req.params.id, {
      include: [
        { model: Artist, as: 'artist', attributes: ['id', 'name', 'avatar_url'] },
        { model: Song, as: 'songs', attributes: ['id'] }
      ]
    });

    if (!album) {
      return next(new AppError('Album not found', 404));
    }

    const artistName = album.artist?.name || 'Unknown artist';
    const songCount = album.songs?.length || 0;
    renderSharePage(req, res, {
      title: album.title,
      description: `${artistName} - ${songCount} ${songCount === 1 ? 'song' : 'songs'}`,
      imageUrl: album.cover_url || album.artist?.avatar_url,
      type: 'music.album'
    });
  } catch (error) {
    next(error);
  }
};

exports.shareArtist = async (req, res, next) => {
  try {
    const artist = await Artist.findByPk(req.params.id, {
      include: [{ model: Song, as: 'songs', attributes: ['id'] }]
    });

    if (!artist) {
      return next(new AppError('Artist not found', 404));
    }

    const songCount = artist.songs?.length || 0;
    renderSharePage(req, res, {
      title: artist.name,
      description: artist.bio || `${songCount} ${songCount === 1 ? 'song' : 'songs'} on ${APP_NAME}`,
      imageUrl: artist.avatar_url,
      type: 'profile'
    });
  } catch (error) {
    next(error);
  }
};

exports.shareUser = async (req, res, next) => {
  try {
    const user = await User.findByPk(req.params.id, {
      attributes: ['id', 'display_name', 'avatar_url']
    });

    if (!user) {
      return next(new AppError('User not found', 404));
    }

    renderSharePage(req, res, {
      title: user.display_name,
      description: `Profile on ${APP_NAME}`,
      imageUrl: user.avatar_url,
      type: 'profile'
    });
  } catch (error) {
    next(error);
  }
};

exports.sharePlaylist = async (req, res, next) => {
  try {
    const playlist = await Playlist.findByPk(req.params.id, {
      include: [
        { model: User, as: 'user', attributes: ['id', 'display_name', 'avatar_url'] },
        { model: Song, as: 'songs', attributes: ['id', 'cover_url'] }
      ]
    });

    if (!playlist || !playlist.is_public) {
      return next(new AppError('Playlist not found', 404));
    }

    const songCount = playlist.songs?.length || 0;
    renderSharePage(req, res, {
      title: playlist.name,
      description: `${playlist.user?.display_name || APP_NAME} - ${songCount} ${songCount === 1 ? 'song' : 'songs'}`,
      imageUrl: playlist.cover_url || playlist.songs?.find((song) => song.cover_url)?.cover_url || playlist.user?.avatar_url,
      type: 'website'
    });
  } catch (error) {
    next(error);
  }
};
