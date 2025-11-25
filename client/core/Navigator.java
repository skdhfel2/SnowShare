package core;

import javax.swing.*;
import java.awt.*;

/**
 * 화면 이동 전담 클래스
 * Navigator.goTo("Home") 형태로 사용
 */
public class Navigator {
    private static Navigator instance;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    private Navigator() {
        // 싱글톤 패턴
    }
    
    public static Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }
    
    /**
     * BaseFrame에서 호출하여 mainContentPanel 설정
     */
    public void setMainContentPanel(JPanel panel) {
        this.mainContentPanel = panel;
        if (panel.getLayout() instanceof CardLayout) {
            this.cardLayout = (CardLayout) panel.getLayout();
        }
    }
    
    /**
     * 화면 이동
     * @param screenName 화면 이름 (예: "Home", "Login", "News" 등)
     */
    public void goTo(String screenName) {
        if (cardLayout != null && mainContentPanel != null) {
            cardLayout.show(mainContentPanel, screenName);
        } else {
            System.err.println("Navigator가 초기화되지 않았습니다. BaseFrame에서 setMainContentPanel을 호출하세요.");
        }
    }
    
    /**
     * 현재 화면 이름 반환 (필요시 사용)
     */
    public String getCurrentScreen() {
        // CardLayout은 현재 카드 이름을 직접 반환하는 메서드가 없으므로
        // 필요시 각 패널에서 관리하거나 다른 방식으로 구현
        return null;
    }
}

