const express = require('express');
const reviewController = require('../controllers/reviewController');

const router = express.Router();

// 모든 후기 조회
router.get('/', reviewController.getAllReviews);

// 제설함별 후기 조회
router.get('/saltbox/:saltboxId', reviewController.getReviewsBySaltboxId);

// 후기 상세 조회
router.get('/:id', reviewController.getReviewById);

// 후기 작성
router.post('/', reviewController.createReview);

// 후기 수정
router.put('/:id', reviewController.updateReview);

// 후기 삭제
router.delete('/:id', reviewController.deleteReview);

module.exports = router;
