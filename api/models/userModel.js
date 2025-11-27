const bcrypt = require('bcrypt');
const db = require('../lib/db');
const logger = require('../lib/logger');

/**
 * 사용자 모델
 */
class UserModel {
  /**
   * 사용자명으로 사용자 조회
   */
  static async getUserByUsername(username) {
    try {
      const [rows] = await db.query(
        `SELECT id, username, password, created_at, updated_at
         FROM users 
         WHERE username = ?`,
        [username],
      );
      return rows[0] || null;
    } catch (error) {
      logger.error('Error getting user by username:', error);
      throw error;
    }
  }

  /**
   * ID로 사용자 조회 (비밀번호 제외)
   */
  static async getUserById(id) {
    try {
      const [rows] = await db.query(
        `SELECT id, username, created_at, updated_at
         FROM users 
         WHERE id = ?`,
        [id],
      );
      return rows[0] || null;
    } catch (error) {
      logger.error('Error getting user by id:', error);
      throw error;
    }
  }

  /**
   * 회원가입 - 사용자 생성
   */
  static async createUser(userData) {
    try {
      const { username, password } = userData;

      // 비밀번호 해싱
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(password, saltRounds);

      const [result] = await db.query(
        `INSERT INTO users (username, password) 
         VALUES (?, ?)`,
        [username, hashedPassword],
      );
      return result.insertId;
    } catch (error) {
      logger.error('Error creating user:', error);
      throw error;
    }
  }

  /**
   * 비밀번호 검증
   */
  static async verifyPassword(plainPassword, hashedPassword) {
    try {
      return await bcrypt.compare(plainPassword, hashedPassword);
    } catch (error) {
      logger.error('Error verifying password:', error);
      throw error;
    }
  }

  /**
   * 사용자명 중복 확인
   */
  static async checkUserExists(username) {
    try {
      const [rows] = await db.query(
        `SELECT id FROM users 
         WHERE username = ?`,
        [username],
      );
      return rows.length > 0;
    } catch (error) {
      logger.error('Error checking user exists:', error);
      throw error;
    }
  }
}

module.exports = UserModel;
