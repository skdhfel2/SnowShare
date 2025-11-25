require('dotenv').config();

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
      .filter((stmt) => {
        // 빈 문장 제거 및 주석만 있는 줄 제거
        if (stmt.length === 0) return false;
        // 주석만 있는 줄 제거 (--로 시작하는 줄)
        const lines = stmt.split('\n').map((line) => line.trim());
        const hasNonComment = lines.some(
          (line) => line.length > 0 && !line.startsWith('--'),
        );
        return hasNonComment;
      });

    logger.info(`Found ${statements.length} SQL statements to execute`);

    const connection = await db.getConnection();

    for (const [index, statement] of statements.entries()) {
      if (statement) {
        try {
          const statementNum = index + 1;
          logger.info(
            `Executing SQL statement ${statementNum}/${statements.length}`,
          );
          await connection.query(statement);
          logger.info(`Successfully executed statement ${statementNum}`);
        } catch (stmtError) {
          logger.error(`Error executing statement ${index + 1}:`, stmtError);
          logger.error(`Failed statement: ${statement.substring(0, 100)}...`);
          throw stmtError;
        }
      }
    }

    // 생성된 테이블 확인
    const [allTables] = await connection.query('SHOW TABLES');
    const tableNames = allTables.map((row) => Object.values(row)[0]);
    logger.info(`Created tables: ${tableNames.join(', ')}`);

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
