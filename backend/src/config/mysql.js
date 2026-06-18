const { Sequelize } = require('sequelize');

const sequelize = new Sequelize(
    process.env.MYSQL_DATABASE,
    process.env.MYSQL_USER,
    process.env.MYSQL_PASSWORD,
    {
        host: process.env.MYSQL_HOST,
        port: process.env.MYSQL_PORT || 3306,
        dialect: 'mysql',
        logging: false,
        pool: {
            max: 5,
            min: 0,
            acquire: 30000,
            idle: 10000
        }
    }
);

const connectMySQL = async () => {
    try {
        await sequelize.authenticate();
        console.log('MySQL connected');
        await sequelize.sync({ alter: true });
        console.log('MySQL tables synced');
    } catch (err) {
        console.error('MySQL connection error:', err.message);
        process.exit(1);
    }
};

module.exports = { sequelize, connectMySQL };
