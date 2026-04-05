const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Favorite = sequelize.define('Favorite', {
  user_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    allowNull: false,
    field: 'user_id'
  },
  song_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    allowNull: false,
    field: 'song_id'
  },
  created_at: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'created_at'
  }
}, {
  tableName: 'favorites',
  timestamps: false,
  indexes: [
    {
      name: 'idx_user_id',
      fields: ['user_id']
    },
    {
      name: 'idx_song_id',
      fields: ['song_id']
    }
  ]
});

module.exports = Favorite;
