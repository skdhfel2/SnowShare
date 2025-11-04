const db = require('../lib/db');
const logger = require('../lib/logger');

const testConnection = async (req, res) => {
  try {
    const connection = await db.getConnection();
    const [rows] = await connection.query('SELECT 1 as test');
    connection.release();

    logger.info('Database connection test successful');
    res.json({
      success: true,
      message: 'Database connection successful',
      data: rows,
    });
  } catch (error) {
    logger.error('Database connection test failed:', error.message);
    res.status(500).json({
      success: false,
      message: 'Database connection failed',
      error: error.message,
    });
  }
};

module.exports = {
  testConnection,
};

