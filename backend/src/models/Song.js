const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Song = sequelize.define('Song', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  title: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  artist_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    field: 'artist_id'
  },
  album_id: {
    type: DataTypes.INTEGER,
    allowNull: true,
    field: 'album_id'
  },
  audio_url: {
    type: DataTypes.STRING(512),
    allowNull: false
  },
  duration: {
    type: DataTypes.INTEGER,
    allowNull: false,
    comment: 'Duration in seconds'
  },
  cover_url: {
    type: DataTypes.STRING(512),
    allowNull: true
  },
  genre: {
    type: DataTypes.STRING(100),
    allowNull: true
  },
  lyrics: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  play_count: {
    type: DataTypes.INTEGER,
    defaultValue: 0
  },
  created_at: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'created_at'
  },
  updated_at: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'updated_at'
  }
}, {
  tableName: 'songs',
  timestamps: true,
  createdAt: 'created_at',
  updatedAt: 'updated_at',
  indexes: [
    {
      name: 'idx_artist_id',
      fields: ['artist_id']
    },
    {
      name: 'idx_album_id',
      fields: ['album_id']
    },
    {
      name: 'idx_title',
      fields: ['title']
    },
    {
      name: 'idx_genre',
      fields: ['genre']
    }
  ]
});

module.exports = Song;
