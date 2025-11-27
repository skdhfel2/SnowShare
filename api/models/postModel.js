const { pool: db } = require('../lib/db');
const logger = require('../lib/logger');

/**
 * 자유게시판 모델
 */
class PostModel {
  /**
   * 모든 게시글 조회
   */
  static async getAllPosts() {
    try {
      const [rows] = await db.query(
        `SELECT p.*,
                u.username,
                (
                  SELECT COUNT(*) 
                  FROM comments c 
                  WHERE c.post_id = p.id 
                    AND c.post_type = 'post'
                ) AS comment_count
         FROM posts p
         LEFT JOIN users u ON p.user_id = u.id
         ORDER BY p.created_at DESC`,
      );
      return rows;
    } catch (error) {
      logger.error('Error getting all posts:', error);
      throw error;
    }
  }

  /**
   * 게시글 ID로 조회
   */
  static async getPostById(id) {
    try {
      const [rows] = await db.query(
        `SELECT p.*,
                u.username,
                (
                  SELECT COUNT(*) 
                  FROM comments c 
                  WHERE c.post_id = p.id 
                    AND c.post_type = 'post'
                ) AS comment_count
         FROM posts p
         LEFT JOIN users u ON p.user_id = u.id
         WHERE p.id = ?`,
        [id],
      );
      return rows[0] || null;
    } catch (error) {
      logger.error('Error getting post by id:', error);
      throw error;
    }
  }

  /**
   * 게시글 작성
   */
  static async createPost(postData) {
    try {
      const { title, content, user_id } = postData;
      const [result] = await db.query(
        `INSERT INTO posts (title, content, user_id) 
         VALUES (?, ?, ?)`,
        [title, content, user_id],
      );
      return result.insertId;
    } catch (error) {
      logger.error('Error creating post:', error);
      throw error;
    }
  }

  /**
   * 게시글 수정
   */
  static async updatePost(id, postData, userId) {
    try {
      const { title, content } = postData;
      const [result] = await db.query(
        `UPDATE posts 
         SET title = ?, content = ?, updated_at = CURRENT_TIMESTAMP 
         WHERE id = ? AND user_id = ?`,
        [title, content, id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error updating post:', error);
      throw error;
    }
  }

  /**
   * 게시글 삭제 (단순 삭제만 수행)
   * 트랜잭션은 service 레이어에서 관리합니다.
   */
  static async deletePost(id, userId) {
    try {
      const [result] = await db.query(
        `DELETE FROM posts WHERE id = ? AND user_id = ?`,
        [id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error deleting post:', error);
      throw error;
    }
  }

  /**
   * 작성자 확인
   */
  static async isAuthor(postId, userId) {
    try {
      const [rows] = await db.query(
        `SELECT id FROM posts WHERE id = ? AND user_id = ?`,
        [postId, userId],
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
        `UPDATE posts 
         SET view_count = view_count + 1 
         WHERE id = ?`,
        [id],
      );
    } catch (error) {
      logger.error('Error incrementing post view count:', error);
      throw error;
    }
  }
}

module.exports = PostModel;
