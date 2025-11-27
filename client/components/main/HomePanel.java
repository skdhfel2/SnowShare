package components.main;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 홈 화면 (시스템 소개 문구)
 */
public class HomePanel extends BasePanel {
    
    public HomePanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        
        String introHtml = "<html><center>"
                + "<font size='6'>안녕하세요.</font><br><br>"
                + "<font size='6'>저희 SnowShare는</font><br><br><br>"
                + "폭설 시 시민과 소상공인이 함께 제설 작업에 참여할 수 있도록<br><br>"
                + "제설함 위치, 용품 잔량, 폭설 관련 정보를 한눈에 제공하는<br><br>"
                + "시민 참여형 제설용품 공유 플랫폼입니다."
                + "</center></html>";
        
        JLabel introLabel = new JLabel(introHtml);
        introLabel.setFont(FONT_INTRO_BODY);
        introLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        GridBagConstraints gbc = new GridBagConstraints();
        add(introLabel, gbc);
    }
}

