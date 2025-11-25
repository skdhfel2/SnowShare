const express = require('express');
const commentController = require('../controllers/commentController');

const router = express.Router();

// 게시글의 댓글 목록 조회
router.get('/post/:postId', commentController.getCommentsByPostId);

// 댓글 상세 조회
router.get('/:id', commentController.getCommentById);

// 댓글 작성
router.post('/', commentController.createComment);

// 댓글 수정
router.put('/:id', commentController.updateComment);

// 댓글 삭제
router.delete('/:id', commentController.deleteComment);

module.exports = router;
