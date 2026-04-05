const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Album = sequelize.define('Album', {
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
  cover_url: {
    type: DataTypes.STRING(512),
    allowNull: true
  },
  release_date: {
    type: DataTypes.DATEONLY,
    allowNull: true
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
  tableName: 'albums',
  timestamps: true,
  createdAt: 'created_at',
  updatedAt: 'updated_at',
  indexes: [
    {
      name: 'idx_artist_id',
      fields: ['artist_id']
    },
    {
      name: 'idx_title',
      fields: ['title']
    }
  ]
});

module.exports = Album;
