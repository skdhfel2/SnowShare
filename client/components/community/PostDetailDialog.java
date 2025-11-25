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
 * 게시글 상세보기 다이얼로그 (수정/삭제 포함)
 */
public class PostDetailDialog extends JDialog {
    private Post post;
    private JLabel titleLabel;
    private JTextArea contentArea;
    private JLabel authorLabel;
    private JLabel dateLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton closeButton;
    private CommentPanel commentPanel;
    private boolean refreshNeeded = false;
    
    public PostDetailDialog(JFrame parent, Post post) {
        super(parent, "게시글 상세보기", true);
        this.post = post;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // 상단: 게시글 정보
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // 제목
        titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(core.BasePanel.FONT_TITLE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 작성자 및 날짜
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authorLabel = new JLabel("작성자: " + (post.getUsername().isEmpty() ? post.getUserId() : post.getUsername()));
        authorLabel.setFont(core.BasePanel.FONT_BODY);
        dateLabel = new JLabel("작성일: " + post.getCreatedAt());
        dateLabel.setFont(core.BasePanel.FONT_BODY);
        metaPanel.add(authorLabel);
        metaPanel.add(Box.createHorizontalStrut(20));
        metaPanel.add(dateLabel);
        
        // 내용
        contentArea = new JTextArea(post.getContent());
        contentArea.setFont(core.BasePanel.FONT_BODY);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(getBackground());
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createTitledBorder("내용"));
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(metaPanel, BorderLayout.CENTER);
        infoPanel.add(contentScroll, BorderLayout.SOUTH);
        
        // 중앙: 댓글 패널
        commentPanel = new CommentPanel(post.getId(), "post");
        
        // 하단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // 본인 게시글인 경우 수정/삭제 버튼 표시
        String currentUserId = Session.getInstance().getUserId();
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            editButton = new JButton("수정");
            deleteButton = new JButton("삭제");
            
            editButton.addActionListener(e -> editPost());
            deleteButton.addActionListener(e -> deletePost());
            
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
        }
        
        closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(infoPanel, BorderLayout.NORTH);
        add(commentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void editPost() {
        PostWriteDialog dialog = new PostWriteDialog((JFrame) getParent(), post);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            // 게시글 다시 로드
            try {
                JSONObject response = ApiClient.get("/posts/" + post.getId());
                if (response.optBoolean("success", false)) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        post = new Post(data);
                        titleLabel.setText(post.getTitle());
                        contentArea.setText(post.getContent());
                        dateLabel.setText("작성일: " + post.getCreatedAt());
                        refreshNeeded = true;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "게시글을 다시 불러오는데 실패했습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deletePost() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "정말 이 게시글을 삭제하시겠습니까?",
            "삭제 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                JSONObject data = new JSONObject();
                data.put("user_id", Session.getInstance().getUserId());
                
                JSONObject response = ApiClient.delete("/posts/" + post.getId(), data);
                
                if (response.optBoolean("success", false)) {
                    JOptionPane.showMessageDialog(this, 
                        "게시글이 삭제되었습니다.",
                        "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshNeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        response.optString("message", "삭제에 실패했습니다."),
                        "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "서버 연결에 실패했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }
}








