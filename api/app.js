const express = require('express');
const cors = require('cors');
require('dotenv').config();

const logger = require('./lib/logger');
const { initDatabase } = require('./models/initDatabase');
const testRoutes = require('./routes/test');
const postRoutes = require('./routes/posts');
const reviewRoutes = require('./routes/reviews');
const commentRoutes = require('./routes/comments');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  logger.info(`${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/api/test', testRoutes);
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
    await initDatabase();
    logger.info('Database initialization completed');

    app.listen(PORT, () => {
      logger.info(`Server is running on port ${PORT}`);
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

startServer();

module.exports = app;
