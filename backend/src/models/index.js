const sequelize = require('../config/database');

// Import all models
const User = require('./User');
const Artist = require('./Artist');
const Album = require('./Album');
const Song = require('./Song');
const Playlist = require('./Playlist');
const PlaylistSong = require('./PlaylistSong');
const UserLibrarySong = require('./UserLibrarySong');
const RefreshToken = require('./RefreshToken');

// Define relationships

// User relationships
User.hasMany(Playlist, { foreignKey: 'user_id', as: 'playlists' });
User.hasMany(UserLibrarySong, { foreignKey: 'user_id', as: 'librarySongLinks' });
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
Song.hasMany(UserLibrarySong, { foreignKey: 'song_id', as: 'libraryLinks' });
Song.belongsToMany(User, {
  through: UserLibrarySong,
  foreignKey: 'song_id',
  otherKey: 'user_id',
  as: 'libraryUsers'
});

// Playlist relationships
Playlist.belongsTo(User, { foreignKey: 'user_id', as: 'user' });
Playlist.belongsToMany(Song, { 
  through: PlaylistSong, 
  foreignKey: 'playlist_id',
  otherKey: 'song_id',
  as: 'songs' 
});

// Personal library relationships
UserLibrarySong.belongsTo(User, { foreignKey: 'user_id', as: 'user' });
UserLibrarySong.belongsTo(Song, { foreignKey: 'song_id', as: 'song' });
User.belongsToMany(Song, {
  through: UserLibrarySong,
  foreignKey: 'user_id',
  otherKey: 'song_id',
  as: 'librarySongs'
});

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

const ensureAccountLibrarySchema = async () => {
  const [roleColumns] = await sequelize.query(`
    SELECT COLUMN_NAME
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
    LIMIT 1;
  `);

  if (roleColumns.length === 0) {
    await sequelize.query(`
      ALTER TABLE \`users\`
      ADD COLUMN \`role\` VARCHAR(32) NOT NULL DEFAULT 'user'
      AFTER \`avatar_url\`;
    `);
  }

  await sequelize.query(`
    UPDATE \`users\`
    SET \`role\` = 'admin'
    WHERE LOWER(\`email\`) = 'admin@example.com';
  `);

  await sequelize.query(`
    CREATE TABLE IF NOT EXISTS \`user_library_songs\` (
      \`user_id\` INT NOT NULL,
      \`song_id\` INT NOT NULL,
      \`created_at\` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (\`user_id\`, \`song_id\`),
      CONSTRAINT \`user_library_songs_user_fk\`
        FOREIGN KEY (\`user_id\`) REFERENCES \`users\`(\`id\`) ON DELETE CASCADE,
      CONSTRAINT \`user_library_songs_song_fk\`
        FOREIGN KEY (\`song_id\`) REFERENCES \`songs\`(\`id\`) ON DELETE CASCADE,
      INDEX \`idx_user_library_user_id\` (\`user_id\`),
      INDEX \`idx_user_library_song_id\` (\`song_id\`),
      INDEX \`idx_user_library_created_at\` (\`created_at\`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
  `);

  const [legacyTables] = await sequelize.query(`
    SELECT TABLE_NAME
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorites'
    LIMIT 1;
  `);

  if (legacyTables.length > 0) {
    await sequelize.query(`
      INSERT IGNORE INTO \`user_library_songs\` (\`user_id\`, \`song_id\`, \`created_at\`)
      SELECT f.\`user_id\`, f.\`song_id\`, COALESCE(f.\`created_at\`, CURRENT_TIMESTAMP)
      FROM \`favorites\` f
      INNER JOIN \`users\` u ON u.\`id\` = f.\`user_id\`
      INNER JOIN \`songs\` s ON s.\`id\` = f.\`song_id\`;
    `);
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
  UserLibrarySong,
  RefreshToken,
  syncDatabase,
  ensureAccountLibrarySchema
};
