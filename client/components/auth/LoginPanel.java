package components.auth;

import core.BasePanel;
import core.Session;
import core.Navigator;
import utils.ApiClient;
import org.json.JSONObject;
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
        
        // 로그인 버튼 이벤트
        loginButton.addActionListener(e -> {
            String username = idField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("ID와 비밀번호를 입력해주세요.");
                return;
            }
            
            // 로그인 처리 (별도 스레드에서 실행)
            loginButton.setEnabled(false);
            new Thread(() -> {
                try {
                    JSONObject requestData = new JSONObject();
                    requestData.put("username", username);
                    requestData.put("password", password);
                    
                    JSONObject response = ApiClient.post("/auth/login", requestData);
                    
                    SwingUtilities.invokeLater(() -> {
                        loginButton.setEnabled(true);
                        
                        if (response.has("success") && response.getBoolean("success")) {
                            // 로그인 성공
                            JSONObject userData = response.getJSONObject("data").getJSONObject("user");
                            Session session = Session.getInstance();
                            session.login(
                                String.valueOf(userData.getInt("id")),
                                userData.getString("username")
                            );
                            
                            showInfo("로그인 성공!");
                            
                            // 홈 화면으로 이동
                            Navigator.getInstance().goTo("Home");
                            
                            // 입력 필드 초기화
                            idField.setText("");
                            passwordField.setText("");
                        } else {
                            // 로그인 실패
                            String message = response.has("message") 
                                ? response.getString("message") 
                                : "로그인에 실패했습니다.";
                            showError(message);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        loginButton.setEnabled(true);
                        showError("로그인 중 오류가 발생했습니다: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                }
            }).start();
        });
    }
}

