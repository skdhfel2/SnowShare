const logger = require('../lib/logger');

/**
 * 세션 기반 인증 미들웨어
 * 세션에 저장된 사용자 정보를 확인하여 인증 상태를 검증
 */
function authMiddleware(req, res, next) {
  try {
    // 세션에 사용자 정보가 있는지 확인
    if (!req.session || !req.session.userId) {
      return res.status(401).json({
        success: false,
        message: '로그인이 필요합니다.',
      });
    }

    // 세션에서 사용자 정보를 req.user에 저장 (기존 코드와의 호환성을 위해)
    req.user = {
      id: req.session.userId,
      username: req.session.username,
    };

    next();
  } catch (error) {
    logger.error('Auth middleware error:', error);
    return res.status(401).json({
      success: false,
      message: '인증에 실패했습니다.',
    });
  }
}

module.exports = authMiddleware;
