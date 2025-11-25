const db = require('../lib/db');
const logger = require('../lib/logger');
const ReviewModel = require('../models/reviewModel');

/**
 * 후기 서비스
 * 트랜잭션 관리 및 비즈니스 로직 처리
 */
class ReviewService {
  /**
   * 후기 삭제 (트랜잭션 포함)
   * 후기와 관련 댓글을 함께 삭제합니다.
   */
  static async deleteReview(id, userId) {
    const connection = await db.getConnection();

    try {
      // 트랜잭션 시작
      await connection.beginTransaction();

      // 작성자 확인
      const isAuthor = await ReviewModel.isAuthor(id, userId);
      if (!isAuthor) {
        await connection.rollback();
        connection.release();
        return {
          success: false,
          message: '본인의 후기만 삭제할 수 있습니다.',
        };
      }

      // 관련 댓글 삭제
      await connection.query(
        `DELETE FROM comments WHERE post_id = ? AND post_type = 'review'`,
        [id],
      );

      // 후기 삭제
      const [result] = await connection.query(
        `DELETE FROM reviews WHERE id = ? AND user_id = ?`,
        [id, userId],
      );

      if (result.affectedRows === 0) {
        await connection.rollback();
        connection.release();
        return {
          success: false,
          message: '후기를 찾을 수 없습니다.',
        };
      }

      // 트랜잭션 커밋
      await connection.commit();
      connection.release();

      logger.info(`Review deleted: ${id} by user ${userId}`);
      return {
        success: true,
        message: '후기가 삭제되었습니다.',
      };
    } catch (error) {
      // 트랜잭션 롤백
      await connection.rollback();
      connection.release();
      logger.error('Error deleting review:', error);
      throw error;
    }
  }
}

module.exports = ReviewService;
