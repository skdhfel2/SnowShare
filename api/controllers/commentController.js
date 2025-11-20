const CommentModel = require('../models/commentModel');
const logger = require('../lib/logger');

/**
 * 댓글 컨트롤러
 */
const commentController = {
  /**
   * 게시글의 댓글 목록 조회
   */
  getCommentsByPostId: async (req, res) => {
    try {
      const { postId } = req.params;
      const { postType = 'post' } = req.query;

      const comments = await CommentModel.getCommentsByPostId(postId, postType);

      res.json({
        success: true,
        data: comments,
      });
    } catch (error) {
      logger.error('Error in getCommentsByPostId:', error);
      res.status(500).json({
        success: false,
        message: '댓글 목록 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 댓글 상세 조회
   */
  getCommentById: async (req, res) => {
    try {
      const { id } = req.params;
      const comment = await CommentModel.getCommentById(id);

      if (!comment) {
        return res.status(404).json({
          success: false,
          message: '댓글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        data: comment,
      });
    } catch (error) {
      logger.error('Error in getCommentById:', error);
      res.status(500).json({
        success: false,
        message: '댓글 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 댓글 작성
   */
  createComment: async (req, res) => {
    try {
      const {
        post_id,
        post_type = 'post',
        user_id,
        content,
        parent_comment_id,
      } = req.body;

      if (!post_id || !user_id || !content) {
        return res.status(400).json({
          success: false,
          message: '게시글 ID, 사용자 ID, 내용은 필수입니다.',
        });
      }

      const commentId = await CommentModel.createComment({
        post_id,
        post_type,
        user_id,
        content,
        parent_comment_id,
      });

      res.status(201).json({
        success: true,
        message: '댓글이 작성되었습니다.',
        data: { id: commentId },
      });
    } catch (error) {
      logger.error('Error in createComment:', error);
      res.status(500).json({
        success: false,
        message: '댓글 작성 실패',
        error: error.message,
      });
    }
  },

  /**
   * 댓글 수정
   */
  updateComment: async (req, res) => {
    try {
      const { id } = req.params;
      const { content, user_id } = req.body;

      if (!content || !user_id) {
        return res.status(400).json({
          success: false,
          message: '내용, 사용자 ID는 필수입니다.',
        });
      }

      // 작성자 확인
      const isAuthor = await CommentModel.isAuthor(id, user_id);
      if (!isAuthor) {
        return res.status(403).json({
          success: false,
          message: '본인의 댓글만 수정할 수 있습니다.',
        });
      }

      const success = await CommentModel.updateComment(id, content, user_id);

      if (!success) {
        return res.status(404).json({
          success: false,
          message: '댓글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        message: '댓글이 수정되었습니다.',
      });
    } catch (error) {
      logger.error('Error in updateComment:', error);
      res.status(500).json({
        success: false,
        message: '댓글 수정 실패',
        error: error.message,
      });
    }
  },

  /**
   * 댓글 삭제
   */
  deleteComment: async (req, res) => {
    try {
      const { id } = req.params;
      const { user_id } = req.body;

      if (!user_id) {
        return res.status(400).json({
          success: false,
          message: '사용자 ID는 필수입니다.',
        });
      }

      // 작성자 확인
      const isAuthor = await CommentModel.isAuthor(id, user_id);
      if (!isAuthor) {
        return res.status(403).json({
          success: false,
          message: '본인의 댓글만 삭제할 수 있습니다.',
        });
      }

      const success = await CommentModel.deleteComment(id, user_id);

      if (!success) {
        return res.status(404).json({
          success: false,
          message: '댓글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        message: '댓글이 삭제되었습니다.',
      });
    } catch (error) {
      logger.error('Error in deleteComment:', error);
      res.status(500).json({
        success: false,
        message: '댓글 삭제 실패',
        error: error.message,
      });
    }
  },
};

module.exports = commentController;
