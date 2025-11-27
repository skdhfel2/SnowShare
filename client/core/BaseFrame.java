package core;

import javax.swing.*;
import java.awt.*;
import components.common.HeaderNav;

/**
 * 앱 전체를 감싸는 기본 프레임 (JPanel 위에 올라가는 최상위 창)
 * - 윈도우 크기
 * - 전체 레이아웃 기본 설정
 * - 기본 스타일(폰트, 여백)
 */
public class BaseFrame extends JFrame {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    
    private JPanel mainContentPanel;
    private HeaderNav headerNav;
    
    public BaseFrame() {
        initializeFrame();
        setupLayout();
    }
    
    private void initializeFrame() {
        setTitle("SnowShare (제설 용품 공유 시스템)");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // ContentPane에 BorderLayout 설정
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
    }
    
    private void setupLayout() {
        Container contentPane = getContentPane();
        
        // 상단 헤더 네비게이션 추가 (모든 페이지에서 고정)
        headerNav = new HeaderNav();
        headerNav.setVisible(true);  // 명시적으로 보이도록 설정
        contentPane.add(headerNav, BorderLayout.NORTH);
        
        // 중앙 콘텐츠 영역 (Navigator가 관리)
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setOpaque(true);
        contentPane.add(mainContentPanel, BorderLayout.CENTER);
    }
    
    public JPanel getMainContentPanel() {
        return mainContentPanel;
    }
    
    public HeaderNav getHeaderNav() {
        return headerNav;
    }
}

