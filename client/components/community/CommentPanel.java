package components.community;

import models.Comment;
import utils.ApiClient;
import core.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 패널 (게시글/후기 상세보기에 포함)
 */
public class CommentPanel extends JPanel {
    private int postId;
    private String postType;
    private String postAuthorId;
    private JPanel commentListPanel;
    private JTextArea commentInputArea;
    private JButton submitButton;
    private JButton refreshButton;
    private List<Comment> comments;
    
    /**
     * @param postId       댓글이 달리는 게시글/후기 ID
     * @param postType     "post" 또는 "review"
     * @param postAuthorId 게시글/후기 작성자 ID (본인 댓글에 '글쓴이' 표시용)
     */
    public CommentPanel(int postId, String postType, String postAuthorId) {
        this.postId = postId;
        this.postType = postType;
        this.postAuthorId = postAuthorId;
        this.comments = new ArrayList<>();
        initializePanel();
        loadComments();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("댓글"));
        
        // 상단: 댓글 목록
        commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));
        commentListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(commentListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        // 하단: 댓글 입력 패널
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        commentInputArea = new JTextArea(3, 30);
        commentInputArea.setFont(core.BasePanel.FONT_BODY);
        commentInputArea.setLineWrap(true);
        commentInputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(commentInputArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("새로고침");
        submitButton = new JButton("댓글 작성");
        
        refreshButton.addActionListener(e -> loadComments());
        submitButton.addActionListener(e -> submitComment());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(submitButton);
        
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 댓글 목록 로드
     */
    private void loadComments() {
        SwingUtilities.invokeLater(() -> {
            try {
                JSONObject response = ApiClient.get("/comments/post/" + postId + "?postType=" + postType);
                if (response.optBoolean("success", false)) {
                    JSONArray data = response.optJSONArray("data");
                    comments.clear();
                    commentListPanel.removeAll();
                    
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            Comment comment = new Comment(data.getJSONObject(i));
                            comments.add(comment);
                            addCommentToPanel(comment, 0);
                        }
                    } else {
                        JLabel noCommentLabel = new JLabel("댓글이 없습니다.");
                        noCommentLabel.setFont(core.BasePanel.FONT_BODY);
                        noCommentLabel.setForeground(Color.GRAY);
                        noCommentLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
                        commentListPanel.add(noCommentLabel);
                    }
                    
                    commentListPanel.revalidate();
                    commentListPanel.repaint();
                } else {
                    showError("댓글 목록을 불러오는데 실패했습니다.");
                }
            } catch (IOException e) {
                showError("서버 연결에 실패했습니다: " + e.getMessage());
            }
        });
    }
    
    /**
     * 댓글을 패널에 추가 (재귀적으로 대댓글도 추가)
     */
    private void addCommentToPanel(Comment comment, int depth) {
        JPanel commentItemPanel = createCommentItem(comment, depth);
        commentListPanel.add(commentItemPanel);
        
        // 대댓글 추가
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                addCommentToPanel(reply, depth + 1);
            }
        }
    }
    
    /**
     * 댓글 아이템 패널 생성
     */
    private JPanel createCommentItem(Comment comment, int depth) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // depth가 깊어질수록 왼쪽 패딩을 늘려 대댓글 구조를 시각적으로 구분
        int leftPadding = 5 + depth * 20;
        panel.setBorder(new EmptyBorder(5, leftPadding, 5, 5));
        panel.setBackground(Color.WHITE);
        
        // 댓글 정보 (작성자, 날짜)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        String baseAuthorName = comment.getUsername().isEmpty()
                ? comment.getUserId()
                : comment.getUsername();

        // 게시글/후기 작성자가 단 댓글이면 닉네임 오른쪽에 [글쓴이] 표시
        // depth가 1 이상인 대댓글은 별도의 아이콘(↳)을 닉네임 왼쪽에 표시해 시각적으로 구분
        if (depth > 0) {
            JLabel replyIcon = new JLabel("↳");
            replyIcon.setFont(core.BasePanel.FONT_BODY.deriveFont(Font.BOLD));
            replyIcon.setForeground(Color.GRAY);
            infoPanel.add(replyIcon);
        }

        boolean isPostAuthor =
                postAuthorId != null && !postAuthorId.isEmpty()
                        && postAuthorId.equals(comment.getUserId());

        if (isPostAuthor) {
            String html = String.format(
                    "<html>%s <span style='color:#FF6600;font-size:10px;'>[글쓴이]</span></html>",
                    baseAuthorName
            );
            JLabel authorLabel = new JLabel(html);
            authorLabel.setFont(core.BasePanel.FONT_BODY);
            authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
            infoPanel.add(authorLabel);
        } else {
            JLabel authorLabel = new JLabel(baseAuthorName);
            authorLabel.setFont(core.BasePanel.FONT_BODY);
            authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
            infoPanel.add(authorLabel);
        }
        
        String dateStr = comment.getCreatedAt();
        if (dateStr != null && dateStr.length() > 16) {
            dateStr = dateStr.substring(0, 16);
        }
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(core.BasePanel.FONT_BODY);
        dateLabel.setForeground(Color.GRAY);
        
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(dateLabel);
        
        // 댓글 내용 (본문보다 한 단계 낮은 시각적 중요도로 표시)
        JTextArea contentArea = new JTextArea(comment.getContent());
        float contentFontSize = core.BasePanel.FONT_BODY.getSize2D() - 1f;
        contentArea.setFont(core.BasePanel.FONT_BODY.deriveFont(contentFontSize));
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        // 버튼 패널 (답글, 수정, 삭제)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        String currentUserId = Session.getInstance().getUserId();
        
        // 답글 버튼 (최상위 댓글에서만 허용, 대댓글에는 비활성화)
        if (depth == 0) {
            JButton replyButton = new JButton("답글");
            replyButton.setFont(core.BasePanel.FONT_BODY);
            replyButton.addActionListener(e -> openReplyDialog(comment));
            buttonPanel.add(replyButton);
        }
        
        // 본인 댓글인 경우 수정/삭제 버튼 표시
        if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
            JButton editButton = new JButton("수정");
            editButton.setFont(core.BasePanel.FONT_BODY);
            // 수정 버튼: 강조색 (파랑 계열)
            editButton.setBackground(new Color(0xE3F2FD));
            editButton.setForeground(new Color(0x1565C0));
            editButton.setFocusPainted(false);
            editButton.addActionListener(e -> editComment(comment));
            
            JButton deleteButton = new JButton("삭제");
            deleteButton.setFont(core.BasePanel.FONT_BODY);
            // 삭제 버튼: 경고색 (빨강 계열)
            deleteButton.setBackground(new Color(0xFFEBEE));
            deleteButton.setForeground(new Color(0xC62828));
            deleteButton.setFocusPainted(false);
            deleteButton.addActionListener(e -> deleteComment(comment));
            
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 댓글 작성
     */
    private void submitComment() {
        String content = commentInputArea.getText().trim();
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "댓글 내용을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = Session.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("post_id", postId);
            data.put("post_type", postType);
            data.put("user_id", userId);
            data.put("content", content);
            
            JSONObject response = ApiClient.post("/comments", data);
            
            if (response.optBoolean("success", false)) {
                commentInputArea.setText("");
                loadComments();
            } else {
                JOptionPane.showMessageDialog(this, 
                    response.optString("message", "댓글 작성에 실패했습니다."),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "서버 연결에 실패했습니다: " + e.getMessage(),
                "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 답글 작성 다이얼로그
     */
    private void openReplyDialog(Comment parentComment) {
        String content = JOptionPane.showInputDialog(
            this,
            "답글을 입력하세요:",
            "답글 작성",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (content != null && !content.trim().isEmpty()) {
            String userId = Session.getInstance().getUserId();
            if (userId == null || userId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                JSONObject data = new JSONObject();
                data.put("post_id", postId);
                data.put("post_type", postType);
                data.put("user_id", userId);
                data.put("content", content.trim());
                data.put("parent_comment_id", parentComment.getId());
                
                JSONObject response = ApiClient.post("/comments", data);
                
                if (response.optBoolean("success", false)) {
                    loadComments();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        response.optString("message", "답글 작성에 실패했습니다."),
                        "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "서버 연결에 실패했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 댓글 수정
     */
    private void editComment(Comment comment) {
        // 기존 댓글 내용을 기본값으로 갖는 입력 다이얼로그
        String newContent = (String) JOptionPane.showInputDialog(
            this,
            "댓글을 수정하세요:",
            comment.getContent()
        );
        
        if (newContent != null && !newContent.trim().isEmpty()) {
            try {
                JSONObject data = new JSONObject();
                data.put("content", newContent.trim());
                data.put("user_id", Session.getInstance().getUserId());
                
                JSONObject response = ApiClient.put("/comments/" + comment.getId(), data);
                
                if (response.optBoolean("success", false)) {
                    loadComments();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        response.optString("message", "댓글 수정에 실패했습니다."),
                        "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "서버 연결에 실패했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 댓글 삭제
     */
    private void deleteComment(Comment comment) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "정말 이 댓글을 삭제하시겠습니까?",
            "삭제 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                JSONObject data = new JSONObject();
                data.put("user_id", Session.getInstance().getUserId());
                
                JSONObject response = ApiClient.delete("/comments/" + comment.getId(), data);
                
                if (response.optBoolean("success", false)) {
                    loadComments();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        response.optString("message", "댓글 삭제에 실패했습니다."),
                        "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "서버 연결에 실패했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }
}








