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

// CORS
app.use(
  cors({
    origin: process.env.CORS_ORIGIN?.split(','),
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

// Routes
app.use('/api/test', testRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/reviews', reviewRoutes);
app.use('/api/comments', commentRoutes);

// Health Check
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
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

// 🚀 서버 시작
async function startServer() {
  try {
    await initDatabase();
    logger.info('Database initialized');

    // 세션 스토어 생성 (DB 연결 이후)
    const sessionStore = new MySQLStore(
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

    app.listen(PORT, '0.0.0.0', () => {
      logger.info(`Server running on port ${PORT}`);
    });
  } catch (error) {
    logger.error('Server start error:', error);
    // 종료 ❌ 절대 안 함 (Railway crash 방지)
  }
}

startServer();

module.exports = app;
