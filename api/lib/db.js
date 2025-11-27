require('dotenv').config();
const mysql = require('mysql2/promise');
const logger = require('./logger');

// Railway MySQL 서비스 환경 변수 우선순위:
// 1. DB_* (사용자가 직접 설정한 값, 예: DB_HOST = mysql.railway.internal)
// 2. MYSQLHOST, MYSQLPORT, MYSQLUSER, MYSQLPASSWORD, MYSQLDATABASE (Railway 자동 제공)
// 3. RAILWAY_PRIVATE_DOMAIN (Railway private domain)
// 4. fallback 값
const dbConfig = {
  host:
    process.env.DB_HOST ||
    process.env.MYSQLHOST ||
    process.env.RAILWAY_PRIVATE_DOMAIN ||
    'localhost',
  port: Number(
    process.env.DB_PORT ||
      process.env.MYSQLPORT ||
      process.env.MYSQL_PORT ||
      '3306',
  ),
  user:
    process.env.DB_USER ||
    process.env.MYSQLUSER ||
    process.env.MYSQL_USER ||
    'root',
  password:
    process.env.DB_PASS ||
    process.env.DB_PASSWORD ||
    process.env.MYSQLPASSWORD ||
    process.env.MYSQL_PASSWORD ||
    '',
  database:
    process.env.DB_NAME ||
    process.env.MYSQLDATABASE ||
    process.env.MYSQL_DATABASE ||
    'snowshare',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
};

const pool = mysql.createPool(dbConfig);

async function initDB() {
  try {
    logger.info('Attempting to connect to MySQL...');
    logger.info('Connection config:', {
      host: dbConfig.host,
      port: dbConfig.port,
      user: dbConfig.user,
      database: dbConfig.database,
      hasPassword: !!dbConfig.password,
    });

    // 연결 테스트
    await pool.query('SELECT 1');

    logger.info('🚀 MySQL Connected!');
    logger.info('HOST:', dbConfig.host);
    logger.info('PORT:', dbConfig.port);
    logger.info('DATABASE:', dbConfig.database);

    return pool;
  } catch (err) {
    logger.error('❌ MySQL Connection FAILED!');
    logger.error('Error Code:', err.code);
    logger.error('Error Number:', err.errno);
    logger.error('SQL State:', err.sqlState);
    logger.error('Error Message:', err.message);
    logger.error('Full Error:', {
      code: err.code,
      errno: err.errno,
      sqlState: err.sqlState,
      message: err.message,
      stack: err.stack,
    });
    logger.error('Connection Config Used:', {
      host: dbConfig.host,
      port: dbConfig.port,
      user: dbConfig.user,
      database: dbConfig.database,
      hasPassword: !!dbConfig.password,
    });
    throw err; // 서버가 잘못된 DB 설정으로 계속 실행되지 않도록
  }
}

// 정석 export 방식: pool과 initDB를 명확하게 분리
module.exports = { pool, initDB };
