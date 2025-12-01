const express = require('express');

const router = express.Router();
const logger = require('../lib/logger');

/**
 * POST /api/summarize
 * 텍스트를 Google Gemini API로 요약
 *
 * Request Body:
 * {
 *   "text": "요약할 텍스트"
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "summary": "요약된 텍스트"
 * }
 */
router.post('/', async (req, res) => {
  try {
    const { text } = req.body;

    // 입력 검증
    if (!text || typeof text !== 'string' || text.trim().length === 0) {
      return res.status(400).json({
        success: false,
        error: '요약할 텍스트가 필요합니다.',
      });
    }

    // 환경변수에서 API 키 읽기
    const apiKey = process.env.GEMINI_API_KEY;
    const model = process.env.GEMINI_MODEL || 'gemini-2.0-flash';
    const baseUrl =
      process.env.GEMINI_API_URL ||
      'https://generativelanguage.googleapis.com/v1beta/models';

    if (!apiKey) {
      logger.error('GEMINI_API_KEY is not configured');
      return res.status(500).json({
        success: false,
        error: 'AI 서비스 설정이 올바르지 않습니다.',
      });
    }

    // Gemini API 호출
    const url = `${baseUrl}/${model}:generateContent?key=${apiKey}`;

    const requestBody = {
      contents: [
        {
          parts: [
            {
              text: `다음 뉴스 기사를 한국어로 간단하고 명확하게 3-5문장으로 요약해주세요:\n\n${text}`,
            },
          ],
        },
      ],
    };

    logger.info('Calling Gemini API for summarization');

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    });

    const data = await response.json();

    if (!response.ok) {
      logger.error('Gemini API error:', data);
      return res.status(response.status).json({
        success: false,
        error: data.error?.message || 'AI 요약 서비스 호출에 실패했습니다.',
      });
    }

    // 응답에서 요약 텍스트 추출
    if (
      data.candidates &&
      data.candidates.length > 0 &&
      data.candidates[0].content &&
      data.candidates[0].content.parts &&
      data.candidates[0].content.parts.length > 0
    ) {
      const summary = data.candidates[0].content.parts[0].text;

      logger.info('Successfully generated summary');

      return res.json({
        success: true,
        summary,
      });
    }

    // 예상치 못한 응답 구조
    logger.error('Unexpected Gemini API response structure:', data);
    return res.status(500).json({
      success: false,
      error: 'AI 응답 형식이 올바르지 않습니다.',
    });
  } catch (error) {
    logger.error('Error in summarize route:', error);
    return res.status(500).json({
      success: false,
      error: '요약 처리 중 오류가 발생했습니다.',
    });
  }
});

module.exports = router;
