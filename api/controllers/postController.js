const PostModel = require('../models/postModel');
const logger = require('../lib/logger');

/**
 * 자유게시판 컨트롤러
 */
const postController = {
  /**
   * 모든 게시글 조회
   */
  getAllPosts: async (req, res) => {
    try {
      const posts = await PostModel.getAllPosts();
      res.json({
        success: true,
        data: posts,
      });
    } catch (error) {
      logger.error('Error in getAllPosts:', error);
      res.status(500).json({
        success: false,
        message: '게시글 목록 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 게시글 상세 조회
   */
  getPostById: async (req, res) => {
    try {
      const { id } = req.params;
      const post = await PostModel.getPostById(id);

      if (!post) {
        return res.status(404).json({
          success: false,
          message: '게시글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        data: post,
      });
    } catch (error) {
      logger.error('Error in getPostById:', error);
      res.status(500).json({
        success: false,
        message: '게시글 조회 실패',
        error: error.message,
      });
    }
  },

  /**
   * 게시글 작성
   */
  createPost: async (req, res) => {
    try {
      const { title, content, user_id } = req.body;

      if (!title || !content || !user_id) {
        return res.status(400).json({
          success: false,
          message: '제목, 내용, 사용자 ID는 필수입니다.',
        });
      }

      const postId = await PostModel.createPost({ title, content, user_id });

      res.status(201).json({
        success: true,
        message: '게시글이 작성되었습니다.',
        data: { id: postId },
      });
    } catch (error) {
      logger.error('Error in createPost:', error);
      res.status(500).json({
        success: false,
        message: '게시글 작성 실패',
        error: error.message,
      });
    }
  },

  /**
   * 게시글 수정
   */
  updatePost: async (req, res) => {
    try {
      const { id } = req.params;
      const { title, content, user_id } = req.body;

      if (!title || !content || !user_id) {
        return res.status(400).json({
          success: false,
          message: '제목, 내용, 사용자 ID는 필수입니다.',
        });
      }

      // 작성자 확인
      const isAuthor = await PostModel.isAuthor(id, user_id);
      if (!isAuthor) {
        return res.status(403).json({
          success: false,
          message: '본인의 게시글만 수정할 수 있습니다.',
        });
      }

      const success = await PostModel.updatePost(
        id,
        { title, content },
        user_id,
      );

      if (!success) {
        return res.status(404).json({
          success: false,
          message: '게시글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        message: '게시글이 수정되었습니다.',
      });
    } catch (error) {
      logger.error('Error in updatePost:', error);
      res.status(500).json({
        success: false,
        message: '게시글 수정 실패',
        error: error.message,
      });
    }
  },

  /**
   * 게시글 삭제
   */
  deletePost: async (req, res) => {
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
      const isAuthor = await PostModel.isAuthor(id, user_id);
      if (!isAuthor) {
        return res.status(403).json({
          success: false,
          message: '본인의 게시글만 삭제할 수 있습니다.',
        });
      }

      const success = await PostModel.deletePost(id, user_id);

      if (!success) {
        return res.status(404).json({
          success: false,
          message: '게시글을 찾을 수 없습니다.',
        });
      }

      res.json({
        success: true,
        message: '게시글이 삭제되었습니다.',
      });
    } catch (error) {
      logger.error('Error in deletePost:', error);
      res.status(500).json({
        success: false,
        message: '게시글 삭제 실패',
        error: error.message,
      });
    }
  },
};

module.exports = postController;
