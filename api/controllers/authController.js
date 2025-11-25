const UserModel = require('../models/userModel');
const logger = require('../lib/logger');

/**
 * 회원가입
 */
async function register(req, res) {
  try {
    const { username, password } = req.body;

    // 입력 검증
    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: '사용자명과 비밀번호를 모두 입력해주세요.',
      });
    }

    // 비밀번호 길이 검증 (최소 6자)
    if (password.length < 6) {
      return res.status(400).json({
        success: false,
        message: '비밀번호는 최소 6자 이상이어야 합니다.',
      });
    }

    // 사용자명 길이 검증
    if (username.length < 3 || username.length > 50) {
      return res.status(400).json({
        success: false,
        message: '사용자명은 3자 이상 50자 이하여야 합니다.',
      });
    }

    // 중복 확인
    const userExists = await UserModel.checkUserExists(username);
    if (userExists) {
      return res.status(409).json({
        success: false,
        message: '이미 사용 중인 사용자명입니다.',
      });
    }

    // 사용자 생성
    const userId = await UserModel.createUser({
      username,
      password,
    });

    // 생성된 사용자 정보 조회 (비밀번호 제외)
    const user = await UserModel.getUserById(userId);

    // 세션에 사용자 정보 저장
    req.session.userId = user.id;
    req.session.username = user.username;

    logger.info(`User registered: ${username}`);

    res.status(201).json({
      success: true,
      message: '회원가입이 완료되었습니다.',
      data: {
        user: {
          id: user.id,
          username: user.username,
        },
      },
    });
  } catch (error) {
    logger.error('Registration error:', error);

    // MySQL 에러 처리
    if (error.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({
        success: false,
        message: '이미 사용 중인 사용자명입니다.',
      });
    }

    res.status(500).json({
      success: false,
      message: '회원가입 중 오류가 발생했습니다.',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
}

/**
 * 로그인
 */
async function login(req, res) {
  try {
    const { username, password } = req.body;

    // 입력 검증
    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: '사용자명과 비밀번호를 입력해주세요.',
      });
    }

    // 사용자 조회 (사용자명으로)
    const user = await UserModel.getUserByUsername(username);

    if (!user) {
      return res.status(401).json({
        success: false,
        message: '사용자명 또는 비밀번호가 올바르지 않습니다.',
      });
    }

    // 비밀번호 검증
    const isPasswordValid = await UserModel.verifyPassword(
      password,
      user.password,
    );

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: '사용자명 또는 비밀번호가 올바르지 않습니다.',
      });
    }

    // 세션에 사용자 정보 저장
    req.session.userId = user.id;
    req.session.username = user.username;

    logger.info(`User logged in: ${user.username}`);

    res.json({
      success: true,
      message: '로그인 성공',
      data: {
        user: {
          id: user.id,
          username: user.username,
        },
      },
    });
  } catch (error) {
    logger.error('Login error:', error);
    res.status(500).json({
      success: false,
      message: '로그인 중 오류가 발생했습니다.',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
}

/**
 * 현재 사용자 정보 조회 (인증 필요)
 */
async function getMe(req, res) {
  try {
    // 세션에서 사용자 정보 가져오기
    if (!req.session.userId) {
      return res.status(401).json({
        success: false,
        message: '로그인이 필요합니다.',
      });
    }

    const user = await UserModel.getUserById(req.session.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: '사용자를 찾을 수 없습니다.',
      });
    }

    res.json({
      success: true,
      data: {
        user: {
          id: user.id,
          username: user.username,
          created_at: user.created_at,
        },
      },
    });
  } catch (error) {
    logger.error('Get me error:', error);
    res.status(500).json({
      success: false,
      message: '사용자 정보 조회 중 오류가 발생했습니다.',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
}

/**
 * 로그아웃
 */
async function logout(req, res) {
  try {
    const { username } = req.session;

    // 세션 삭제
    req.session.destroy((err) => {
      if (err) {
        logger.error('Session destroy error:', err);
        return res.status(500).json({
          success: false,
          message: '로그아웃 중 오류가 발생했습니다.',
        });
      }

      // 쿠키 삭제
      res.clearCookie('connect.sid');

      logger.info(`User logged out: ${username || 'unknown'}`);

      res.json({
        success: true,
        message: '로그아웃되었습니다.',
      });
    });
  } catch (error) {
    logger.error('Logout error:', error);
    res.status(500).json({
      success: false,
      message: '로그아웃 중 오류가 발생했습니다.',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
}

module.exports = {
  register,
  login,
  getMe,
  logout,
};
