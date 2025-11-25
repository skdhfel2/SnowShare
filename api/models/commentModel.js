const db = require('../lib/db');
const logger = require('../lib/logger');

/**
 * 댓글 모델
 */
class CommentModel {
  /**
   * 게시글의 모든 댓글 조회 (계층 구조)
   */
  static async getCommentsByPostId(postId, postType = 'post') {
    try {
      const [rows] = await db.query(
        `SELECT c.*, u.username 
         FROM comments c 
         LEFT JOIN users u ON c.user_id = u.id 
         WHERE c.post_id = ? AND c.post_type = ? 
         ORDER BY c.created_at ASC`,
        [postId, postType],
      );

      // 부모 댓글과 대댓글을 분리하여 계층 구조 생성
      const parentComments = rows.filter(
        (comment) => !comment.parent_comment_id,
      );
      const childComments = rows.filter((comment) => comment.parent_comment_id);

      // 대댓글을 부모 댓글에 연결
      const commentsWithReplies = parentComments.map((parent) => {
        parent.replies = childComments.filter(
          (child) => child.parent_comment_id === parent.id,
        );
        return parent;
      });

      return commentsWithReplies;
    } catch (error) {
      logger.error('Error getting comments by post id:', error);
      throw error;
    }
  }

  /**
   * 댓글 ID로 조회
   */
  static async getCommentById(id) {
    try {
      const [rows] = await db.query(
        `SELECT c.*, u.username 
         FROM comments c 
         LEFT JOIN users u ON c.user_id = u.id 
         WHERE c.id = ?`,
        [id],
      );
      return rows[0] || null;
    } catch (error) {
      logger.error('Error getting comment by id:', error);
      throw error;
    }
  }

  /**
   * 댓글 작성
   */
  static async createComment(commentData) {
    try {
      const { post_id, post_type, user_id, content, parent_comment_id } =
        commentData;
      const [result] = await db.query(
        `INSERT INTO comments (post_id, post_type, user_id, content, parent_comment_id) 
         VALUES (?, ?, ?, ?, ?)`,
        [post_id, post_type, user_id, content, parent_comment_id || null],
      );
      return result.insertId;
    } catch (error) {
      logger.error('Error creating comment:', error);
      throw error;
    }
  }

  /**
   * 댓글 수정
   */
  static async updateComment(id, content, userId) {
    try {
      const [result] = await db.query(
        `UPDATE comments 
         SET content = ? 
         WHERE id = ? AND user_id = ?`,
        [content, id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error updating comment:', error);
      throw error;
    }
  }

  /**
   * 댓글 삭제
   */
  static async deleteComment(id, userId) {
    try {
      // 대댓글이 있는 경우도 CASCADE로 자동 삭제됨
      const [result] = await db.query(
        `DELETE FROM comments WHERE id = ? AND user_id = ?`,
        [id, userId],
      );
      return result.affectedRows > 0;
    } catch (error) {
      logger.error('Error deleting comment:', error);
      throw error;
    }
  }

  /**
   * 작성자 확인
   */
  static async isAuthor(commentId, userId) {
    try {
      const [rows] = await db.query(
        `SELECT id FROM comments WHERE id = ? AND user_id = ?`,
        [commentId, userId],
      );
      return rows.length > 0;
    } catch (error) {
      logger.error('Error checking author:', error);
      throw error;
    }
  }
}

module.exports = CommentModel;
