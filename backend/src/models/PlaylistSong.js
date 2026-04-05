const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const PlaylistSong = sequelize.define('PlaylistSong', {
  playlist_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    allowNull: false,
    field: 'playlist_id'
  },
  song_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    allowNull: false,
    field: 'song_id'
  },
  position: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  added_at: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'added_at'
  }
}, {
  tableName: 'playlist_songs',
  timestamps: false,
  indexes: [
    {
      name: 'idx_playlist_id',
      fields: ['playlist_id']
    },
    {
      name: 'idx_song_id',
      fields: ['song_id']
    }
  ]
});

module.exports = PlaylistSong;
