package components.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import core.Navigator;

/**
 * 상단 헤더 네비게이션 (모든 페이지에서 고정으로 사용)
 * - 로고
 * - 탭 버튼 (관련뉴스, 대응안내, 제설함 지도, 커뮤니티)
 * - 인증 버튼 (로그인, 회원가입)
 */
public class HeaderNav extends JPanel {
    private static final Color COLOR_NAV_BACKGROUND = new Color(45, 52, 54);
    private static final Color COLOR_NAV_TEXT = Color.WHITE;
    private static final Color COLOR_NAV_HOVER = new Color(99, 110, 114);
    private static final Font FONT_LOGO = new Font("SansSerif", Font.BOLD, 20);
    private static final Font FONT_NAV = new Font("SansSerif", Font.BOLD, 14);
    
    private Navigator navigator;
    
    public HeaderNav() {
        this.navigator = Navigator.getInstance();
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_NAV_BACKGROUND);
        
        // 로고 (WEST)
        JLabel logoLabel = createLogoLabel();
        add(logoLabel, BorderLayout.WEST);
        
        // 중앙 탭 버튼 패널 (CENTER)
        JPanel tabButtonPanel = createTabButtonPanel();
        add(tabButtonPanel, BorderLayout.CENTER);
        
        // 오른쪽 인증 패널 (EAST)
        JPanel authPanel = createAuthPanel();
        add(authPanel, BorderLayout.EAST);
    }
    
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel(" ❄️ SnowShare");
        logoLabel.setFont(FONT_LOGO);
        logoLabel.setForeground(COLOR_NAV_TEXT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // 로고 클릭 이벤트 (홈으로)
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigator.goTo("Home");
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                logoLabel.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        return logoLabel;
    }
    
    private JPanel createTabButtonPanel() {
        JPanel tabButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        tabButtonPanel.setOpaque(false);
        
        JButton btnNews = new JButton("관련뉴스");
        JButton btnGuide = new JButton("대응안내");
        JButton btnMap = new JButton("제설함 지도");
        JButton btnCommunity = new JButton("커뮤니티");
        
        styleNavButton(btnNews);
        styleNavButton(btnGuide);
        styleNavButton(btnMap);
        styleNavButton(btnCommunity);
        
        // 클릭 이벤트
        btnNews.addActionListener(e -> navigator.goTo("News"));
        btnGuide.addActionListener(e -> navigator.goTo("Guide"));
        btnMap.addActionListener(e -> navigator.goTo("Map"));
        btnCommunity.addActionListener(e -> navigator.goTo("Community"));
        
        tabButtonPanel.add(btnNews);
        tabButtonPanel.add(btnGuide);
        tabButtonPanel.add(btnMap);
        tabButtonPanel.add(btnCommunity);
        
        return tabButtonPanel;
    }
    
    private JPanel createAuthPanel() {
        JPanel authPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        authPanel.setOpaque(false);
        authPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JButton btnLogin = new JButton("로그인");
        JButton btnRegister = new JButton("회원가입");
        
        styleNavButton(btnLogin);
        styleNavButton(btnRegister);
        
        // 클릭 이벤트
        btnLogin.addActionListener(e -> navigator.goTo("Login"));
        btnRegister.addActionListener(e -> navigator.goTo("Register"));
        
        authPanel.add(btnLogin);
        authPanel.add(btnRegister);
        
        return authPanel;
    }
    
    private void styleNavButton(JButton button) {
        button.setFont(FONT_NAV);
        button.setForeground(COLOR_NAV_TEXT);
        button.setBackground(COLOR_NAV_BACKGROUND);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(COLOR_NAV_HOVER);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_NAV_BACKGROUND);
            }
        });
    }
}

