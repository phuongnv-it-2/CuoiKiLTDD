const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/mysql');

const User = sequelize.define('User', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    deviceId: {
        type: DataTypes.STRING(100),
        allowNull: true,
        unique: true,
        field: 'device_id'
    },
    email: {
        type: DataTypes.STRING(150),
        allowNull: true,
        unique: true
    },
    passwordHash: {
        type: DataTypes.STRING(255),
        allowNull: true,
        field: 'password_hash'
    },
    displayName: {
        type: DataTypes.STRING(100),
        allowNull: true,
        field: 'display_name'
    },
    createdAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW,
        field: 'created_at'
    }
}, {
    tableName: 'users',
    timestamps: false
});

const SearchHistory = sequelize.define('SearchHistory', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    userId: {
        type: DataTypes.INTEGER,
        allowNull: true,
        field: 'user_id',
        references: { model: User, key: 'id' }
    },
    sessionId: {
        type: DataTypes.STRING(100),
        allowNull: false,
        field: 'session_id'
    },
    query: {
        type: DataTypes.STRING(500),
        allowNull: false
    },
    mode: {
        type: DataTypes.ENUM('SEARCH', 'SHOPPING', 'TRANSLATE', 'TEXT', 'QR'),
        allowNull: false
    },
    resultCount: {
        type: DataTypes.INTEGER,
        defaultValue: 0,
        field: 'result_count'
    },
    aiResultId: {
        type: DataTypes.STRING(50),
        allowNull: true,
        field: 'ai_result_id'
    },
    createdAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW,
        field: 'created_at'
    }
}, {
    tableName: 'search_history',
    timestamps: false
});

const Favorite = sequelize.define('Favorite', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    userId: {
        type: DataTypes.INTEGER,
        allowNull: false,
        field: 'user_id',
        references: { model: User, key: 'id' }
    },
    aiResultId: {
        type: DataTypes.STRING(50),
        allowNull: false,
        field: 'ai_result_id'
    },
    label: {
        type: DataTypes.STRING(255),
        allowNull: true
    },
    createdAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW,
        field: 'created_at'
    }
}, {
    tableName: 'favorites',
    timestamps: false
});

User.hasMany(SearchHistory, { foreignKey: 'user_id' });
SearchHistory.belongsTo(User, { foreignKey: 'user_id' });

User.hasMany(Favorite, { foreignKey: 'user_id' });
Favorite.belongsTo(User, { foreignKey: 'user_id' });

module.exports = { User, SearchHistory, Favorite };