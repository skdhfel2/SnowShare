const logger = require('../lib/logger');

const errorHandler = (err, req, res, _next) => {
  logger.error(`Error: ${err.message}`, { stack: err.stack });

  const status = err.status || err.statusCode || 500;
  const message = err.message || 'Internal Server Error';

  res.status(status).json({
    success: false,
    error: {
      message,
      status,
      ...(process.env.NODE_ENV !== 'production' && { stack: err.stack }),
    },
  });
};

module.exports = errorHandler;
