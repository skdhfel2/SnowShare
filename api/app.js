const express = require('express');
const cors = require('cors');
const session = require('express-session');
const MySQLStore = require('express-mysql-session')(session);
require('dotenv').config();

const logger = require('./lib/logger');
const db = require('./lib/db');
const { initDatabase } = require('./models/initDatabase');

const testRoutes = require('./routes/test');
const authRoutes = require('./routes/auth');
const postRoutes = require('./routes/posts');
const reviewRoutes = require('./routes/reviews');
const commentRoutes = require('./routes/comments');

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
let sessionStore;
try {
  sessionStore = new MySQLStore(
    {
      host: process.env.DB_HOST,
      port: process.env.DB_PORT,
      user: process.env.DB_USER,
      password: process.env.DB_PASS,
      database: process.env.DB_NAME,
    },
    db,
  );

  app.use(
    session({
      secret: process.env.SESSION_SECRET || 'default',
      store: sessionStore,
      resave: false,
      saveUninitialized: false,
      cookie: { maxAge: 86400000, httpOnly: true },
    }),
  );
} catch (sessionError) {
  logger.error('Session store initialization error:', sessionError);
  // 세션 스토어 실패해도 메모리 세션으로 계속 진행
  app.use(
    session({
      secret: process.env.SESSION_SECRET || 'default',
      resave: false,
      saveUninitialized: false,
      cookie: { maxAge: 86400000, httpOnly: true },
    }),
  );
}

// Routes
app.use('/api/test', testRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/reviews', reviewRoutes);
app.use('/api/comments', commentRoutes);

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
app.listen(PORT, '0.0.0.0', () => {
  logger.info(`Server running on port ${PORT}`);

  // 서버 시작 후 DB 초기화 (비동기)
  initDatabase()
    .then(() => {
      dbInitialized = true;
      dbError = null;
      logger.info('Database initialized successfully');
    })
    .catch((error) => {
      dbError = error;
      logger.error('Database initialization failed:', error);
      logger.error(
        'Server will continue running, but database operations may fail',
      );
      // 서버는 계속 실행 (Railway crash 방지)
    });
});

module.exports = app;
