const express = require('express');
const authController = require('../controllers/authController');
const authMiddleware = require('../middleware/authMiddleware');

const router = express.Router();

/**
 * @route   POST /api/auth/register
 * @desc    회원가입
 * @access  Public
 */
router.post('/register', authController.register);

/**
 * @route   POST /api/auth/login
 * @desc    로그인
 * @access  Public
 */
router.post('/login', authController.login);

/**
 * @route   GET /api/auth/me
 * @desc    현재 사용자 정보 조회
 * @access  Private (인증 필요)
 */
router.get('/me', authMiddleware, authController.getMe);

/**
 * @route   POST /api/auth/logout
 * @desc    로그아웃
 * @access  Private (인증 필요)
 */
router.post('/logout', authMiddleware, authController.logout);

module.exports = router;

