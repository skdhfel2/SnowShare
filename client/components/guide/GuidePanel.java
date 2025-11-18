package components.guide;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 대응안내 화면 (폭설 대응 안내)
 */
public class GuidePanel extends BasePanel {
    
    public GuidePanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        
        JLabel label = new JLabel("대응안내 콘텐츠가 표시될 영역입니다.");
        label.setFont(FONT_INTRO_BODY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        add(label, gbc);
    }
}

