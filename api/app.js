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

// 세션 스토어 설정 (MySQL)
const sessionStore = new MySQLStore(
  {
    host: process.env.DB_HOST || process.env.MYSQL_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || process.env.MYSQLPORT || '3306', 10),
    user: process.env.DB_USER || process.env.MYSQLUSER || 'root',
    password: process.env.DB_PASS || process.env.MYSQLPASSWORD || '',
    database: process.env.DB_NAME || process.env.MYSQLDATABASE || 'snowshare',
    clearExpired: true,
    checkExpirationInterval: 900000, // 15분마다 만료된 세션 확인
    expiration: 86400000, // 24시간 (밀리초)
  },
  db,
);

// 세션 미들웨어 설정
app.use(
  session({
    secret:
      process.env.SESSION_SECRET ||
      'your-session-secret-key-change-in-production',
    store: sessionStore,
    resave: false,
    saveUninitialized: false,
    cookie: {
      maxAge: 86400000, // 24시간
      httpOnly: true, // XSS 공격 방지
      secure: process.env.NODE_ENV === 'production', // HTTPS에서만 전송 (프로덕션)
      sameSite: 'lax', // CSRF 공격 방지
    },
  }),
);

// CORS 설정 (세션 쿠키를 위해 credentials 허용)
app.use(
  cors({
    origin: process.env.CORS_ORIGIN || true,
    credentials: true, // 쿠키 전송 허용
  }),
);

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  logger.info(`${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/api/test', testRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/reviews', reviewRoutes);
app.use('/api/comments', commentRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Server is running' });
});

// Error handling middleware
app.use((err, req, res, _next) => {
  logger.error(err.stack);
  res.status(err.status || 500).json({
    error: {
      message: err.message || 'Internal Server Error',
      status: err.status || 500,
    },
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: { message: 'Route not found', status: 404 } });
});

// 서버 시작 전 데이터베이스 초기화
async function startServer() {
  try {
    // 데이터베이스 초기화
    await initDatabase();
    logger.info('Database initialization completed');

    // 서버 시작 (Railway 환경에 맞게 0.0.0.0으로 바인딩)
    app.listen(PORT, '0.0.0.0', () => {
      logger.info(`Server is running on port ${PORT}`);
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

// 서버 시작
startServer();

module.exports = app;
