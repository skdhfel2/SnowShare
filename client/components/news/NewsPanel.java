package components.news;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 관련뉴스 화면
 */
public class NewsPanel extends BasePanel {
    
    public NewsPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        
        JLabel label = new JLabel("관련 뉴스 콘텐츠가 표시될 영역입니다.");
        label.setFont(FONT_INTRO_BODY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        add(label, gbc);
    }
}

