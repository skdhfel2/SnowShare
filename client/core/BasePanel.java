package core;

import javax.swing.*;
import java.awt.*;

/**
 * 모든 기능 화면(로그인, 지도, 게시판 등)이 이 클래스를 상속해서 만들어짐.
 * - 공통 스타일 정의
 * - 공통 메서드(알림창, API 호출 기본 로직 등)
 */
public class BasePanel extends JPanel {
    // 공통 색상 상수
    protected static final Color COLOR_CONTENT_BACKGROUND = Color.WHITE;
    protected static final Color COLOR_NAV_BACKGROUND = new Color(45, 52, 54);
    protected static final Color COLOR_NAV_TEXT = Color.WHITE;
    protected static final Color COLOR_NAV_HOVER = new Color(99, 110, 114);
    
    // 공통 폰트 상수
    protected static final Font FONT_LOGO = new Font("SansSerif", Font.BOLD, 20);
    protected static final Font FONT_NAV = new Font("SansSerif", Font.BOLD, 14);
    protected static final Font FONT_INTRO_BODY = new Font("SansSerif", Font.PLAIN, 18);
    protected static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    protected static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    
    public BasePanel() {
        setBackground(COLOR_CONTENT_BACKGROUND);
    }
    
    /**
     * 공통 알림창 표시
     */
    protected void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    /**
     * 에러 메시지 표시
     */
    protected void showError(String message) {
        showMessage(message, "오류", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 정보 메시지 표시
     */
    protected void showInfo(String message) {
        showMessage(message, "알림", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 확인 다이얼로그 표시
     */
    protected int showConfirm(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
}

