const mysql = require('mysql2/promise');
const logger = require('./logger');

const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASS || '',
  database: process.env.DB_NAME || 'snowshare',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
};

const pool = mysql.createPool(dbConfig);

// Test connection
pool
  .getConnection()
  .then((connection) => {
    logger.info('MySQL connection pool created successfully');
    connection.release();
  })
  .catch((err) => {
    logger.error('MySQL connection error:', err.message);
  });

module.exports = pool;

