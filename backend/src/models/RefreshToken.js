const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const RefreshToken = sequelize.define('RefreshToken', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  user_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    field: 'user_id'
  },
  token: {
    type: DataTypes.STRING(512),
    unique: true,
    allowNull: false
  },
  expires_at: {
    type: DataTypes.DATE,
    allowNull: false
  },
  created_at: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'created_at'
  }
}, {
  tableName: 'refresh_tokens',
  timestamps: false,
  indexes: [
    {
      name: 'idx_token',
      fields: ['token']
    },
    {
      name: 'idx_user_id',
      fields: ['user_id']
    }
  ]
});

module.exports = RefreshToken;
