const ReviewModel = require('../models/reviewModel');
const ReviewService = require('../services/reviewService');
const logger = require('../lib/logger');

/**
 * 후기게시판 컨트롤러
 */
const reviewController = {
  /**
   * 모든 후기 조회
   */
  getAllReviews: async (req, res) => {
    try {
      const reviews = await ReviewModel.getAllReviews();
      res.json({
        success: true,
        data: reviews,
      });
    } catch (error) {
      logger.error('Error in getAllReviews:', error);
      res.status(500).json({
        success: false,
        message: '후기 목록 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 후기 상세 조회
   */
  getReviewById: async (req, res) => {
    try {
      const { id } = req.params;
      const review = await ReviewModel.getReviewById(id);

      if (!review) {
        return res.status(404).json({
          success: false,
          message: '후기를 찾을 수 없습니다.',
        });
      }

      // 조회수 증가 (에러가 나더라도 응답은 계속 진행)
      try {
        await ReviewModel.incrementViewCount(id);
        review.view_count += 1;
      } catch (incError) {
        logger.error('Failed to increment review view count:', incError);
      }

      res.json({
        success: true,
        data: review,
      });
    } catch (error) {
      logger.error('Error in getReviewById:', error);
      res.status(500).json({
        success: false,
        message: '후기 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 제설함별 후기 조회
   */
  getReviewsBySaltboxId: async (req, res) => {
    try {
      const { saltboxId } = req.params;
      const reviews = await ReviewModel.getReviewsBySaltboxId(saltboxId);

      res.json({
        success: true,
        data: reviews,
      });
    } catch (error) {
      logger.error('Error in getReviewsBySaltboxId:', error);
      res.status(500).json({
        success: false,
        message: '후기 목록 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 후기 작성
   */
  createReview: async (req, res) => {
    try {
      const { saltbox_id, user_id, rating, content } = req.body;

      if (!saltbox_id || !user_id || !rating || !content) {
        return res.status(400).json({
          success: false,
          message: '제설함 ID, 사용자 ID, 별점, 내용은 필수입니다.',
        });
      }

      if (rating < 1 || rating > 5) {
        return res.status(400).json({
          success: false,
          message: '별점은 1~5 사이의 값이어야 합니다.',
        });
      }

      const reviewId = await ReviewModel.createReview({
        saltbox_id,
        user_id,
        rating,
        content,
      });

      res.status(201).json({
        success: true,
        message: '후기가 작성되었습니다.',
        data: { id: reviewId },
      });
    } catch (error) {
      logger.error('Error in createReview:', error);
      res.status(500).json({
        success: false,
        message: '후기 작성 실패',
        error: error.message,
      });
    }
  },

  /**
   * 후기 수정
   */
  updateReview: async (req, res) => {
    try {
      const { id } = req.params;
      const { rating, content, user_id } = req.body;

      if (!rating || !content || !user_id) {
        return res.status(400).json({
          success: false,
          message: '별점, 내용, 사용자 ID는 필수입니다.',
        });
      }

      if (rating < 1 || rating > 5) {
        return res.status(400).json({
          success: false,
          message: '별점은 1~5 사이의 값이어야 합니다.',
        });
      }

      // 작성자 확인
      const isAuthor = await ReviewModel.isAuthor(id, user_id);
      if (!isAuthor) {
        return res.status(403).json({
          success: false,
          message: '본인의 후기만 수정할 수 있습니다.',
        });
      }

      const success = await ReviewModel.updateReview(
        id,
        { rating, content },
        user_id,
      );

      if (!success) {
        return res.status(404).json({
          success: false,
          message: '후기를 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        message: '후기가 수정되었습니다.',
      });
    } catch (error) {
      logger.error('Error in updateReview:', error);
      res.status(500).json({
        success: false,
        message: '후기 수정 실패',
        error: error.message,
      });
    }
  },

  /**
   * 후기 삭제
   */
  deleteReview: async (req, res) => {
    try {
      const { id } = req.params;
      const { user_id } = req.body;

      if (!user_id) {
        return res.status(400).json({
          success: false,
          message: '사용자 ID는 필수입니다.',
        });
      }

      // Service 레이어에서 트랜잭션 처리
      const result = await ReviewService.deleteReview(id, user_id);

      if (!result.success) {
        const statusCode =
          result.message === '본인의 후기만 삭제할 수 있습니다.' ? 403 : 404;
        return res.status(statusCode).json(result);
      }

      res.json(result);
    } catch (error) {
      logger.error('Error in deleteReview:', error);
      res.status(500).json({
        success: false,
        message: '후기 삭제 실패',
        error: error.message,
      });
    }
  },
};

module.exports = reviewController;
