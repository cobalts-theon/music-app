const sequelize = require('../config/database');

// Import all models
const User = require('./User');
const Artist = require('./Artist');
const Album = require('./Album');
const Song = require('./Song');
const Playlist = require('./Playlist');
const PlaylistSong = require('./PlaylistSong');
const Favorite = require('./Favorite');
const RefreshToken = require('./RefreshToken');

// Define relationships

// User relationships
User.hasMany(Playlist, { foreignKey: 'user_id', as: 'playlists' });
User.hasMany(Favorite, { foreignKey: 'user_id', as: 'favorites' });
User.hasMany(RefreshToken, { foreignKey: 'user_id', as: 'refreshTokens' });

// Artist relationships
Artist.hasMany(Album, { foreignKey: 'artist_id', as: 'albums' });
Artist.hasMany(Song, { foreignKey: 'artist_id', as: 'songs' });

// Album relationships
Album.belongsTo(Artist, { foreignKey: 'artist_id', as: 'artist' });
Album.hasMany(Song, { foreignKey: 'album_id', as: 'songs' });

// Song relationships
Song.belongsTo(Artist, { foreignKey: 'artist_id', as: 'artist' });
Song.belongsTo(Album, { foreignKey: 'album_id', as: 'album' });
Song.belongsToMany(Playlist, { 
  through: PlaylistSong, 
  foreignKey: 'song_id',
  otherKey: 'playlist_id',
  as: 'playlists' 
});
Song.hasMany(Favorite, { foreignKey: 'song_id', as: 'favorites' });

// Playlist relationships
Playlist.belongsTo(User, { foreignKey: 'user_id', as: 'user' });
Playlist.belongsToMany(Song, { 
  through: PlaylistSong, 
  foreignKey: 'playlist_id',
  otherKey: 'song_id',
  as: 'songs' 
});

// Favorite relationships
Favorite.belongsTo(User, { foreignKey: 'user_id', as: 'user' });
Favorite.belongsTo(Song, { foreignKey: 'song_id', as: 'song' });

// RefreshToken relationships
RefreshToken.belongsTo(User, { foreignKey: 'user_id', as: 'user' });

// Sync database (only in development)
const syncDatabase = async () => {
  if (process.env.NODE_ENV === 'development') {
    try {
      // alter: true will update tables without dropping them
      await sequelize.sync({ alter: false });
      console.log('upDatabase synchronized');
    } catch (error) {
      console.error('Database sync failed:', error.message);
    }
  }
};

// Export models and sequelize instance
module.exports = {
  sequelize,
  User,
  Artist,
  Album,
  Song,
  Playlist,
  PlaylistSong,
  Favorite,
  RefreshToken,
  syncDatabase
};
