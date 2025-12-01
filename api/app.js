const express = require('express');
const cors = require('cors');
const session = require('express-session');
const MySQLStore = require('express-mysql-session')(session);
require('dotenv').config();

const logger = require('./lib/logger');
const { pool: db, initDB } = require('./lib/db');
const { initDatabase } = require('./models/initDatabase');

const testRoutes = require('./routes/test');
const authRoutes = require('./routes/auth');
const postRoutes = require('./routes/posts');
const reviewRoutes = require('./routes/reviews');
const commentRoutes = require('./routes/comments');
const summarizeRoutes = require('./routes/summarize');

const app = express();
const PORT = process.env.PORT || 3000;

// DB 연결 상태 추적
let dbInitialized = false;
let dbError = null;

// CORS
app.use(
  cors({
    origin: process.env.CORS_ORIGIN ? process.env.CORS_ORIGIN.split(',') : true, // 모든 origin 허용 (개발용)
    credentials: true,
  }),
);

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
  logger.info(`${req.method} ${req.path}`);
  next();
});

// 세션 스토어 생성 및 미들웨어 설정 (라우트 등록 전)
// Railway MySQL 서비스 환경 변수 지원 (db.js와 동일한 우선순위)
const dbHost =
  process.env.DB_HOST ||
  process.env.MYSQLHOST ||
  process.env.RAILWAY_PRIVATE_DOMAIN ||
  'localhost';
const dbPort =
  process.env.DB_PORT ||
  process.env.MYSQLPORT ||
  process.env.MYSQL_PORT ||
  '3306';
const dbUser =
  process.env.DB_USER ||
  process.env.MYSQLUSER ||
  process.env.MYSQL_USER ||
  'root';
const dbPass =
  process.env.DB_PASS ||
  process.env.DB_PASSWORD ||
  process.env.MYSQLPASSWORD ||
  process.env.MYSQL_PASSWORD ||
  '';
const dbName =
  process.env.DB_NAME ||
  process.env.MYSQLDATABASE ||
  process.env.MYSQL_DATABASE ||
  'snowshare';

// 세션 스토어 생성 및 미들웨어 설정 (라우트 등록 전)
// 세션 스토어 초기화 실패해도 메모리 세션으로 계속 진행
let sessionStore;
try {
  sessionStore = new MySQLStore(
    {
      host: dbHost,
      port: dbPort,
      user: dbUser,
      password: dbPass,
      database: dbName,
    },
    db,
  );
  logger.info('MySQL session store initialized');
} catch (sessionError) {
  logger.error('Session store initialization error:', sessionError);
  logger.error('Falling back to memory session');
  sessionStore = null;
}

// 세션 미들웨어 설정
app.use(
  session({
    secret: process.env.SESSION_SECRET || 'default',
    store: sessionStore || undefined, // sessionStore가 null이면 메모리 세션 사용
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 86400000, httpOnly: true },
  }),
);

// DB 연결 상태 확인 미들웨어 (DB가 필요한 API에만 적용)
const checkDatabaseConnection = (req, res, next) => {
  if (!dbInitialized && dbError) {
    return res.status(503).json({
      success: false,
      message: '데이터베이스 연결이 실패했습니다. 잠시 후 다시 시도해주세요.',
      error: 'Database connection failed',
      databaseStatus: 'disconnected',
    });
  }
  if (!dbInitialized) {
    return res.status(503).json({
      success: false,
      message: '데이터베이스 초기화 중입니다. 잠시 후 다시 시도해주세요.',
      error: 'Database initializing',
      databaseStatus: 'initializing',
    });
  }
  next();
};

// Routes
app.use('/api/test', testRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/summarize', summarizeRoutes); // AI 요약 API (DB 불필요)
// DB가 필요한 API에만 미들웨어 적용
app.use('/api/posts', checkDatabaseConnection, postRoutes);
app.use('/api/reviews', checkDatabaseConnection, reviewRoutes);
app.use('/api/comments', checkDatabaseConnection, commentRoutes);

// Root path (Railway health check용)
app.get('/', (req, res) => {
  let databaseStatus;
  if (dbInitialized) {
    databaseStatus = 'connected';
  } else if (dbError) {
    databaseStatus = 'disconnected';
  } else {
    databaseStatus = 'initializing';
  }

  res.json({
    message: 'SnowShare API Server',
    status: 'running',
    database: databaseStatus,
    endpoints: {
      health: '/health',
      api: '/api',
    },
  });
});

// Health Check (DB 상태 포함)
app.get('/health', (req, res) => {
  if (dbInitialized) {
    res.json({ status: 'ok', database: 'connected' });
  } else if (dbError) {
    res.status(503).json({
      status: 'error',
      database: 'disconnected',
      error: dbError.message,
    });
  } else {
    res.status(503).json({ status: 'initializing', database: 'connecting' });
  }
});

// Error handler
app.use((err, req, res, _next) => {
  logger.error(err.stack);
  res.status(err.status || 500).json({
    error: {
      message: err.message,
      status: err.status || 500,
    },
  });
});

// 404
app.use((req, res) => {
  res.status(404).json({ error: { message: 'Route not found', status: 404 } });
});

// 🚀 서버 시작 (먼저 서버를 시작하고, DB 초기화는 비동기로 처리)
try {
  app.listen(PORT, '0.0.0.0', () => {
    logger.info(`Server running on port ${PORT}`);

    // DB 연결 정보 로그 (디버깅용, 비밀번호는 제외)
    logger.info('Database connection config:', {
      host: dbHost,
      port: dbPort,
      user: dbUser,
      database: dbName,
      hasPassword: !!dbPass,
    });

    // 서버 시작 후 DB 초기화 (비동기)
    (async () => {
      try {
        // DB 연결 확인
        await initDB();
        dbInitialized = true;
        dbError = null;
        logger.info('Database connection verified');

        // DB 테이블 초기화
        await initDatabase();
        logger.info('Database tables initialized successfully');
      } catch (error) {
        dbError = error;
        logger.error('Database initialization failed:', error);
        logger.error('Error details:', {
          message: error.message,
          code: error.code,
          errno: error.errno,
          sqlState: error.sqlState,
        });
        logger.error(
          'Server will continue running, but database operations may fail',
        );
        // 서버는 계속 실행 (Railway crash 방지)
      }
    })();
  });
} catch (error) {
  logger.error('Failed to start server:', error);
  logger.error('Error stack:', error.stack);
  process.exit(1);
}

module.exports = app;
