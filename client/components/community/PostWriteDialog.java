package components.community;

import models.Post;
import utils.ApiClient;
import core.Session;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * 게시글 작성/수정 다이얼로그
 */
public class PostWriteDialog extends JDialog {
    private JTextField titleField;
    private JTextArea contentArea;
    private JButton saveButton;
    private JButton cancelButton;
    private Post post;
    private boolean success = false;
    
    public PostWriteDialog(JFrame parent, Post post) {
        super(parent, post == null ? "게시글 작성" : "게시글 수정", true);
        this.post = post;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // 중앙: 입력 필드
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 제목 입력
        JLabel titleLabel = new JLabel("제목:");
        titleLabel.setFont(core.BasePanel.FONT_BODY);
        titleField = new JTextField();
        titleField.setFont(core.BasePanel.FONT_BODY);
        
        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(titleField, BorderLayout.CENTER);
        
        // 내용 입력
        JLabel contentLabel = new JLabel("내용:");
        contentLabel.setFont(core.BasePanel.FONT_BODY);
        contentArea = new JTextArea(15, 40);
        contentArea.setFont(core.BasePanel.FONT_BODY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(contentScroll, BorderLayout.CENTER);
        
        inputPanel.add(titlePanel, BorderLayout.NORTH);
        inputPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 수정 모드인 경우 기존 데이터 로드
        if (post != null) {
            titleField.setText(post.getTitle());
            contentArea.setText(post.getContent());
        }
        
        // 하단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");
        
        saveButton.addActionListener(e -> savePost());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void savePost() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 로그인 확인
        String userId = Session.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("content", content);
            data.put("user_id", userId);
            
            JSONObject response;
            if (post == null) {
                // 작성
                response = ApiClient.post("/posts", data);
            } else {
                // 수정
                response = ApiClient.put("/posts/" + post.getId(), data);
            }
            
            if (response.optBoolean("success", false)) {
                JOptionPane.showMessageDialog(this, 
                    post == null ? "게시글이 작성되었습니다." : "게시글이 수정되었습니다.",
                    "성공", JOptionPane.INFORMATION_MESSAGE);
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    response.optString("message", "작업에 실패했습니다."),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "서버 연결에 실패했습니다: " + e.getMessage(),
                "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}








