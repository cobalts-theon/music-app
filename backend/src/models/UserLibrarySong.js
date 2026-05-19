const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const UserLibrarySong = sequelize.define('UserLibrarySong', {
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
  tableName: 'user_library_songs',
  timestamps: false,
  indexes: [
    {
      name: 'idx_user_library_user_id',
      fields: ['user_id']
    },
    {
      name: 'idx_user_library_song_id',
      fields: ['song_id']
    },
    {
      name: 'idx_user_library_created_at',
      fields: ['created_at']
    }
  ]
});

module.exports = UserLibrarySong;
