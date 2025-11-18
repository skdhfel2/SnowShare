import core.*;
import components.main.HomePanel;
import components.auth.LoginPanel;
import components.auth.RegisterPanel;
import components.news.NewsPanel;
import components.guide.GuidePanel;
import components.map.MapPanel;
import components.community.CommunityPanel;
import javax.swing.*;

/**
 * "화면 전환 관리자"
 * Navigator 사용해서 화면 이동
 * Login → Home → Map 등 변경
 */
public class App {
    private BaseFrame baseFrame;
    private Navigator navigator;
    
    public App() {
        // BaseFrame 생성
        baseFrame = new BaseFrame();
        
        // Navigator 초기화
        navigator = Navigator.getInstance();
        navigator.setMainContentPanel(baseFrame.getMainContentPanel());
        
        // 모든 화면 패널 생성 및 등록
        registerAllPanels();
        
        // 초기 화면 설정 (Home)
        navigator.goTo("Home");
        
        // 프레임 표시
        baseFrame.setVisible(true);
    }
    
    /**
     * 모든 화면 패널을 CardLayout에 등록
     */
    private void registerAllPanels() {
        JPanel mainContentPanel = baseFrame.getMainContentPanel();
        
        // 각 화면 패널 생성 및 추가
        mainContentPanel.add("Home", new HomePanel());
        mainContentPanel.add("Login", new LoginPanel());
        mainContentPanel.add("Register", new RegisterPanel());
        mainContentPanel.add("News", new NewsPanel());
        mainContentPanel.add("Guide", new GuidePanel());
        mainContentPanel.add("Map", new MapPanel());
        mainContentPanel.add("Community", new CommunityPanel());
    }
}

