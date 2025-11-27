package components.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import core.Navigator;
import core.Session;
import utils.IconFactory;

/**
 * 상단 헤더 네비게이션 (모든 페이지에서 고정으로 사용)
 * - 로고
 * - 탭 버튼 (관련뉴스, 대응안내, 제설함 지도, 커뮤니티)
 * - 인증/사용자 영역 (로그인, 회원가입, 사용자명, 로그아웃)
 */
public class HeaderNav extends JPanel {
    // 어두운 회색 계열 색상 (기존 색상)
    private static final Color COLOR_NAV_BACKGROUND = new Color(45, 52, 54);
    private static final Color COLOR_NAV_TEXT = Color.WHITE;
    private static final Color COLOR_NAV_HOVER = new Color(99, 110, 114);
    private static final Color COLOR_NAV_ACTIVE = new Color(70, 80, 85);  // 활성화 시 색상
    private static final Font FONT_LOGO = new Font("맑은 고딕", Font.BOLD, 20);
    private static final Font FONT_NAV = new Font("맑은 고딕", Font.BOLD, 14);

    private Navigator navigator;
    private JPanel authPanel;

    // BaseFrame에서 생성되는 단일 헤더 인스턴스를 다른 곳에서 갱신할 수 있도록 참조 보관
    private static HeaderNav instance;

    public HeaderNav() {
        this.navigator = Navigator.getInstance();
        instance = this;
        initializePanel();
    }

    /**
     * 세션 상태가 바뀌었을 때(로그인/로그아웃) 헤더 우측 영역을 갱신하기 위한 헬퍼
     */
    public static void refreshAuthState() {
        if (instance != null) {
            instance.updateAuthPanel();
        }
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_NAV_BACKGROUND);
        setOpaque(true);  // 불투명도 명시적 설정
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));  // 헤더 높이 보장
        setMinimumSize(new Dimension(0, 60));  // 최소 높이 보장
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));  // 최대 높이 제한
        setVisible(true);  // 명시적으로 보이도록 설정

        // 하단 경계선 추가 (더 명확하게 보이도록)
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(30, 42, 35)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // 로고 (WEST)
        JLabel logoLabel = createLogoLabel();
        add(logoLabel, BorderLayout.WEST);

        // 중앙 탭 버튼 패널 (CENTER)
        JPanel tabButtonPanel = createTabButtonPanel();
        add(tabButtonPanel, BorderLayout.CENTER);

        // 오른쪽 인증/사용자 패널 (EAST)
        authPanel = createAuthPanel();
        add(authPanel, BorderLayout.EAST);

        // 초기 진입 시 세션 상태(로그아웃 상태)에 맞게 버튼 구성
        updateAuthPanel();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // 배경을 직접 그려서 확실하게 표시
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(COLOR_NAV_BACKGROUND);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
    
    private JLabel createLogoLabel() {
        ImageIcon snowIcon = IconFactory.createSnowflakeIcon();
        JLabel logoLabel = new JLabel("SnowShare", snowIcon, SwingConstants.LEFT);
        logoLabel.setFont(FONT_LOGO);
        logoLabel.setForeground(Color.WHITE);  // 명시적으로 흰색 설정
        logoLabel.setOpaque(false);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        logoLabel.setIconTextGap(8);
        
        // 로고 클릭 이벤트 (홈으로)
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigator.goTo("Home");
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                logoLabel.setForeground(new Color(220, 220, 220));  // 호버 시 약간 밝게
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                logoLabel.setCursor(Cursor.getDefaultCursor());
                logoLabel.setForeground(COLOR_NAV_TEXT);
            }
        });
        
        return logoLabel;
    }
    
    private JPanel createTabButtonPanel() {
        JPanel tabButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        tabButtonPanel.setOpaque(false);
        tabButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        CustomNavButton btnNews = new CustomNavButton("관련뉴스", COLOR_NAV_BACKGROUND);
        CustomNavButton btnGuide = new CustomNavButton("대응안내", COLOR_NAV_BACKGROUND);
        CustomNavButton btnMap = new CustomNavButton("제설함 지도", COLOR_NAV_BACKGROUND);
        CustomNavButton btnCommunity = new CustomNavButton("커뮤니티", COLOR_NAV_BACKGROUND);
        
        styleNavButton(btnNews);
        styleNavButton(btnGuide);
        styleNavButton(btnMap);
        styleNavButton(btnCommunity);

        // 클릭 이벤트
        // 대응안내는 비로그인도 접근 가능, 그 외 탭은 로그인 필요
        btnNews.addActionListener(e -> handleProtectedNavigation("News"));
        btnGuide.addActionListener(e -> navigator.goTo("Guide"));
        btnMap.addActionListener(e -> handleProtectedNavigation("Map"));
        btnCommunity.addActionListener(e -> handleProtectedNavigation("Community"));
        
        tabButtonPanel.add(btnNews);
        tabButtonPanel.add(btnGuide);
        tabButtonPanel.add(btnMap);
        tabButtonPanel.add(btnCommunity);
        
        return tabButtonPanel;
    }

    /**
     * 로그인 여부에 따라 우측 인증/사용자 영역을 갱신
     */
    private JPanel createAuthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        return panel;
    }

    private void updateAuthPanel() {
        if (authPanel == null) return;

        authPanel.removeAll();

        Session session = Session.getInstance();
        if (!session.isLoggedIn()) {
            // 비로그인 상태: 로그인/회원가입 버튼
            CustomNavButton btnLogin = new CustomNavButton("로그인", COLOR_NAV_BACKGROUND);
            CustomNavButton btnRegister = new CustomNavButton("회원가입", COLOR_NAV_BACKGROUND);

            styleNavButton(btnLogin);
            styleNavButton(btnRegister);

            btnLogin.addActionListener(e -> navigator.goTo("Login"));
            btnRegister.addActionListener(e -> navigator.goTo("Register"));

            authPanel.add(btnLogin);
            authPanel.add(btnRegister);
        } else {
            // 로그인 상태: 사용자명 + 로그아웃 버튼
            String username = session.getUsername() != null ? session.getUsername() : "사용자";

            JLabel userLabel = new JLabel(username + " 님");
            userLabel.setFont(FONT_NAV);
            userLabel.setForeground(Color.WHITE);
            userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

            CustomNavButton btnLogout = new CustomNavButton("로그아웃", COLOR_NAV_BACKGROUND);
            styleNavButton(btnLogout);
            btnLogout.addActionListener(e -> {
                session.logout();
                JOptionPane.showMessageDialog(
                    this,
                    "로그아웃 되었습니다.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
                navigator.goTo("Home");
                updateAuthPanel();
            });

            authPanel.add(userLabel);
            authPanel.add(btnLogout);
        }

        authPanel.revalidate();
        authPanel.repaint();
    }

    /**
     * 로그인 필요 탭 접근 시 세션 확인
     */
    private void handleProtectedNavigation(String screenName) {
        Session session = Session.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                this,
                "로그인이 필요한 서비스입니다.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE
            );
            navigator.goTo("Login");
            return;
        }
        navigator.goTo(screenName);
    }

    private void styleNavButton(CustomNavButton button) {
        button.setFont(FONT_NAV);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        
        // 부드러운 호버 효과
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCurrentBgColor(COLOR_NAV_HOVER);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCurrentBgColor(COLOR_NAV_BACKGROUND);
                button.repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setCurrentBgColor(COLOR_NAV_ACTIVE);
                button.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setCurrentBgColor(COLOR_NAV_HOVER);
                button.repaint();
            }
        });
    }
    
    // 커스텀 버튼 클래스 - Look and Feel을 무시하고 배경을 직접 그림
    private static class CustomNavButton extends JButton {
        private Color currentBgColor;
        
        public CustomNavButton(String text, Color bgColor) {
            super(text);
            this.currentBgColor = bgColor;
            setOpaque(true);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public void setCurrentBgColor(Color color) {
            this.currentBgColor = color;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(currentBgColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(getForeground());
            FontMetrics fm = g2d.getFontMetrics();
            String text = getText();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(text, x, y);
            g2d.dispose();
        }
    }
}

