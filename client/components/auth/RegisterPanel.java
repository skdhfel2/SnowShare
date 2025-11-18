package components.auth;

import core.BasePanel;
import javax.swing.*;
import java.awt.*;

/**
 * 회원가입 화면 (정보 입력 폼)
 */
public class RegisterPanel extends BasePanel {
    private JTextField idField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    
    public RegisterPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 제목
        JLabel titleLabel = new JLabel("회원가입");
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
        
        // 비밀번호 확인
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("비밀번호 확인:"), gbc);
        
        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);
        
        // 이메일 입력
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("이메일:"), gbc);
        
        emailField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(emailField, gbc);
        
        // 회원가입 버튼
        registerButton = new JButton("회원가입");
        registerButton.setFont(FONT_BODY);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);
        
        // 회원가입 버튼 이벤트 (추후 hooks로 분리 가능)
        registerButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText();
            
            if (id.isEmpty() || password.isEmpty() || email.isEmpty()) {
                showError("모든 필드를 입력해주세요.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showError("비밀번호가 일치하지 않습니다.");
                return;
            }
            
            // TODO: 실제 회원가입 로직 구현 (API 호출)
            showInfo("회원가입 기능은 추후 구현 예정입니다.");
        });
    }
}

