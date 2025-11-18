package components.map;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 제설함 지도 화면 (카카오맵 API)
 */
public class MapPanel extends BasePanel {
    
    public MapPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        
        JLabel label = new JLabel("제설함 지도(카카오맵 API)가 표시될 영역입니다.");
        label.setFont(FONT_INTRO_BODY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        add(label, gbc);
    }
}

