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

    // 기존 users 테이블에서 email 컬럼 제거 (마이그레이션 예시)
    try {
      const [columns] = await connection.query(
        `SELECT COLUMN_NAME 
         FROM information_schema.COLUMNS 
         WHERE TABLE_SCHEMA = DATABASE() 
         AND TABLE_NAME = 'users' 
         AND COLUMN_NAME = 'email'`,
      );
      if (columns.length > 0) {
        logger.info('Removing email column from users table...');
        await connection.query('ALTER TABLE users DROP COLUMN email');
        // 인덱스가 있으면 제거 시도
        try {
          await connection.query('ALTER TABLE users DROP INDEX idx_email');
        } catch (idxError) {
          // 인덱스가 없으면 무시
          logger.info('idx_email index does not exist, skipping...');
        }
        logger.info('Email column removed successfully');
      }
    } catch (migrationError) {
      // 마이그레이션 실패해도 계속 진행 (테이블이 없을 수도 있음)
      logger.info('Migration check completed (table may not exist yet)');
    }

    // posts, reviews 테이블에 view_count 컬럼이 없으면 추가
    try {
      const ensureViewCountColumn = async (tableName) => {
        const [columns] = await connection.query(
          `SELECT COLUMN_NAME 
           FROM information_schema.COLUMNS 
           WHERE TABLE_SCHEMA = DATABASE() 
             AND TABLE_NAME = ? 
             AND COLUMN_NAME = 'view_count'`,
          [tableName],
        );
        if (columns.length === 0) {
          logger.info(`Adding view_count column to ${tableName} table...`);
          await connection.query(
            `ALTER TABLE ${tableName} 
             ADD COLUMN view_count INT NOT NULL DEFAULT 0 COMMENT '조회수'`,
          );
          logger.info(`view_count column added to ${tableName} table`);
        }
      };

      await ensureViewCountColumn('posts');
      await ensureViewCountColumn('reviews');
    } catch (viewColError) {
      logger.error('Error ensuring view_count columns:', viewColError);
    }

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
