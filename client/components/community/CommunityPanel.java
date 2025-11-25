package components.community;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 커뮤니티 화면 (게시판)
 */
public class CommunityPanel extends BasePanel {
    
    public CommunityPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        
        JLabel label = new JLabel("커뮤니티(게시판)가 표시될 영역입니다.");
        label.setFont(FONT_INTRO_BODY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        add(label, gbc);
    }
}

