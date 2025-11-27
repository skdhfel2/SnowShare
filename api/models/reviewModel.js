const db = require('../lib/db');
const logger = require('../lib/logger');

/**
 * 후기게시판 모델
 */
class ReviewModel {
  /**
   * 모든 후기 조회
   */
  static async getAllReviews() {
    try {
      const [rows] = await db.query(
        `SELECT r.*,
                u.username,
                (
                  SELECT COUNT(*) 
                  FROM comments c 
                  WHERE c.post_id = r.id 
                    AND c.post_type = 'review'
                ) AS comment_count
         FROM reviews r
         LEFT JOIN users u ON r.user_id = u.id
         ORDER BY r.created_at DESC`,
      );
      return rows;
    } catch (error) {
      logger.error('Error getting all reviews:', error);
      throw error;
    }
  }

  /**
   * 후기 ID로 조회
   */
  static async getReviewById(id) {
    try {
      const [rows] = await db.query(
        `SELECT r.*,
                u.username,
                (
                  SELECT COUNT(*) 
                  FROM comments c 
                  WHERE c.post_id = r.id 
                    AND c.post_type = 'review'
                ) AS comment_count
         FROM reviews r
         LEFT JOIN users u ON r.user_id = u.id
         WHERE r.id = ?`,
        [id],
      );
      return rows[0] || null;
    } catch (error) {
      logger.error('Error getting review by id:', error);
      throw error;
    }
  }

  /**
   * 제설함별 후기 조회
   */
  static async getReviewsBySaltboxId(saltboxId) {
    try {
      const [rows] = await db.query(
        `SELECT * 
         FROM reviews 
         WHERE saltbox_id = ? 
         ORDER BY created_at DESC`,
        [saltboxId],
      );
      return rows;
    } catch (error) {
      logger.error('Error getting reviews by saltbox id:', error);
      throw error;
    }
  }

  /**
   * 후기 작성
   */
  static async createReview(reviewData) {
    try {
      const { saltbox_id, user_id, rating, content } = reviewData;
      const [result] = await db.query(
        `INSERT INTO reviews (saltbox_id, user_id, rating, content) 
         VALUES (?, ?, ?, ?)`,
        [saltbox_id, user_id, rating, content],
      );
      return result.insertId;
    } catch (error) {
      logger.error('Error creating review:', error);
      throw error;
    }
  }

  /**
   * 후기 수정
   */
  static async updateReview(id, reviewData, userId) {
    try {
      const { rating, content } = reviewData;
      const [result] = await db.query(
        `UPDATE reviews 
         SET rating = ?, content = ? 
         WHERE id = ? AND user_id = ?`,
        [rating, content, id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error updating review:', error);
      throw error;
    }
  }

  /**
   * 후기 삭제 (단순 삭제만 수행)
   * 트랜잭션은 service 레이어에서 관리합니다.
   */
  static async deleteReview(id, userId) {
    try {
      const [result] = await db.query(
        `DELETE FROM reviews WHERE id = ? AND user_id = ?`,
        [id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error deleting review:', error);
      throw error;
    }
  }

  /**
   * 작성자 확인
   */
  static async isAuthor(reviewId, userId) {
    try {
      const [rows] = await db.query(
        `SELECT id FROM reviews WHERE id = ? AND user_id = ?`,
        [reviewId, userId],
      );
      return rows.length > 0;
    } catch (error) {
      logger.error('Error checking author:', error);
      throw error;
    }
  }

  /**
   * 조회수 1 증가
   */
  static async incrementViewCount(id) {
    try {
      await db.query(
        `UPDATE reviews 
         SET view_count = view_count + 1 
         WHERE id = ?`,
        [id],
      );
    } catch (error) {
      logger.error('Error incrementing review view count:', error);
      throw error;
    }
  }
}

module.exports = ReviewModel;
