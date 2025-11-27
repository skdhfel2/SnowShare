package components.community;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 커뮤니티 화면 (게시판)
 * 자유게시판과 후기게시판을 탭으로 표시
 */
public class CommunityPanel extends BasePanel {
    private JTabbedPane tabbedPane;
    private FreeBoardPanel freeBoardPanel;
    private ReviewPanel reviewPanel;
    
    public CommunityPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        
        // 탭 패널 생성
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        // 자유게시판 탭
        freeBoardPanel = new FreeBoardPanel();
        tabbedPane.addTab("자유게시판", freeBoardPanel);
        
        // 후기게시판 탭
        reviewPanel = new ReviewPanel();
        tabbedPane.addTab("후기게시판", reviewPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
}

