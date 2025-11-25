package components.auth;

import core.BasePanel;
import core.Navigator;
import utils.ApiClient;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

/**
 * 회원가입 화면 (정보 입력 폼)
 */
public class RegisterPanel extends BasePanel {
    private JTextField idField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
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
        
        // 회원가입 버튼
        registerButton = new JButton("회원가입");
        registerButton.setFont(FONT_BODY);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);
        
        // 회원가입 버튼 이벤트
        registerButton.addActionListener(e -> {
            String username = idField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // 입력 검증
            if (username.isEmpty() || password.isEmpty()) {
                showError("모든 필드를 입력해주세요.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showError("비밀번호가 일치하지 않습니다.");
                return;
            }
            
            if (password.length() < 6) {
                showError("비밀번호는 최소 6자 이상이어야 합니다.");
                return;
            }
            
            if (username.length() < 3 || username.length() > 50) {
                showError("사용자명은 3자 이상 50자 이하여야 합니다.");
                return;
            }
            
            // 회원가입 처리 (별도 스레드에서 실행)
            registerButton.setEnabled(false);
            new Thread(() -> {
                try {
                    JSONObject requestData = new JSONObject();
                    requestData.put("username", username);
                    requestData.put("password", password);
                    
                    JSONObject response = ApiClient.post("/auth/register", requestData);
                    
                    SwingUtilities.invokeLater(() -> {
                        registerButton.setEnabled(true);
                        
                        if (response.has("success") && response.getBoolean("success")) {
                            // 회원가입 성공
                            showInfo("회원가입이 완료되었습니다!");
                            
                            // 로그인 화면으로 이동
                            Navigator.getInstance().goTo("Login");
                            
                            // 입력 필드 초기화
                            idField.setText("");
                            passwordField.setText("");
                            confirmPasswordField.setText("");
                        } else {
                            // 회원가입 실패
                            String message = response.has("message") 
                                ? response.getString("message") 
                                : "회원가입에 실패했습니다.";
                            showError(message);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        registerButton.setEnabled(true);
                        showError("회원가입 중 오류가 발생했습니다: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                }
            }).start();
        });
    }
}

