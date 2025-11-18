package components.auth;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 로그인 화면 (ID, PW 입력 폼)
 */
public class LoginPanel extends BasePanel {
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public LoginPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 제목
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setFont(FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);
        
        // ID 입력
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("ID:"), gbc);
        
        idField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(idField, gbc);
        
        // 비밀번호 입력
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("비밀번호:"), gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);
        
        // 로그인 버튼
        loginButton = new JButton("로그인");
        loginButton.setFont(FONT_BODY);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(loginButton, gbc);
        
        // 로그인 버튼 이벤트 (추후 hooks로 분리 가능)
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());
            
            if (id.isEmpty() || password.isEmpty()) {
                showError("ID와 비밀번호를 입력해주세요.");
                return;
            }
            
            // TODO: 실제 로그인 로직 구현 (API 호출)
            showInfo("로그인 기능은 추후 구현 예정입니다.");
        });
    }
}

