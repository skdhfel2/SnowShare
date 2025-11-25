const fs = require('fs');
const path = require('path');
const db = require('../lib/db');
const logger = require('../lib/logger');

/**
 * 데이터베이스 테이블 초기화
 */
async function initDatabase() {
  try {
    const sql = fs.readFileSync(
      path.join(__dirname, 'create_tables.sql'),
      'utf8',
    );

    // SQL 문을 세미콜론으로 분리하여 각각 실행
    const statements = sql
      .split(';')
      .map((stmt) => stmt.trim())
      .filter((stmt) => stmt.length > 0 && !stmt.startsWith('--'));

    const connection = await db.getConnection();

    for (const statement of statements) {
      if (statement) {
        await connection.query(statement);
      }
    }

    connection.release();
    logger.info('Database tables initialized successfully');
    return true;
  } catch (error) {
    logger.error('Database initialization failed:', error);
    throw error;
  }
}

// 직접 실행 시
if (require.main === module) {
  initDatabase()
    .then(() => {
      logger.info('Database initialization completed');
      process.exit(0);
    })
    .catch((error) => {
      logger.error('Database initialization failed:', error);
      process.exit(1);
    });
}

module.exports = { initDatabase };
