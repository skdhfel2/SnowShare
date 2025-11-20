const express = require('express');
const postController = require('../controllers/postController');

const router = express.Router();

// 게시글 목록 조회
router.get('/', postController.getAllPosts);

// 게시글 상세 조회
router.get('/:id', postController.getPostById);

// 게시글 작성
router.post('/', postController.createPost);

// 게시글 수정
router.put('/:id', postController.updatePost);

// 게시글 삭제
router.delete('/:id', postController.deletePost);

module.exports = router;
