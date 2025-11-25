package components.community;

import models.Review;
import utils.ApiClient;
import core.Session;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 후기 상세보기 다이얼로그 (수정/삭제 포함)
 */
public class ReviewDetailDialog extends JDialog {
    private Review review;
    private JLabel saltboxLabel;
    private JLabel ratingLabel;
    private JTextArea contentArea;
    private JLabel authorLabel;
    private JLabel dateLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton closeButton;
    private CommentPanel commentPanel;
    private boolean refreshNeeded = false;
    
    /**
     * 문자열을 지정된 횟수만큼 반복하는 헬퍼 메서드 (Java 8 호환)
     */
    private String repeatString(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public ReviewDetailDialog(JFrame parent, Review review) {
        super(parent, "후기 상세보기", true);
        this.review = review;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // 상단: 후기 정보
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // 제설함 ID 및 별점
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saltboxLabel = new JLabel("제설함 ID: " + review.getSaltboxId());
        saltboxLabel.setFont(core.BasePanel.FONT_BODY);
        
        String ratingStr = repeatString("★", review.getRating()) + repeatString("☆", 5 - review.getRating());
        ratingLabel = new JLabel("별점: " + ratingStr + " (" + review.getRating() + "/5)");
        ratingLabel.setFont(core.BasePanel.FONT_BODY);
        
        metaPanel.add(saltboxLabel);
        metaPanel.add(Box.createHorizontalStrut(20));
        metaPanel.add(ratingLabel);
        
        // 작성자 및 날짜
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authorLabel = new JLabel("작성자: " + (review.getUsername().isEmpty() ? review.getUserId() : review.getUsername()));
        authorLabel.setFont(core.BasePanel.FONT_BODY);
        dateLabel = new JLabel("작성일: " + review.getCreatedAt());
        dateLabel.setFont(core.BasePanel.FONT_BODY);
        authorPanel.add(authorLabel);
        authorPanel.add(Box.createHorizontalStrut(20));
        authorPanel.add(dateLabel);
        
        // 내용
        contentArea = new JTextArea(review.getContent());
        contentArea.setFont(core.BasePanel.FONT_BODY);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(getBackground());
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createTitledBorder("후기 내용"));
        
        infoPanel.add(metaPanel, BorderLayout.NORTH);
        infoPanel.add(authorPanel, BorderLayout.CENTER);
        infoPanel.add(contentScroll, BorderLayout.SOUTH);
        
        // 중앙: 댓글 패널
        commentPanel = new CommentPanel(review.getId(), "review");
        
        // 하단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // 본인 후기인 경우 수정/삭제 버튼 표시
        String currentUserId = Session.getInstance().getUserId();
        if (currentUserId != null && currentUserId.equals(review.getUserId())) {
            editButton = new JButton("수정");
            deleteButton = new JButton("삭제");
            
            editButton.addActionListener(e -> editReview());
            deleteButton.addActionListener(e -> deleteReview());
            
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
    
    private void editReview() {
        ReviewWriteDialog dialog = new ReviewWriteDialog((JFrame) getParent(), review);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            // 후기 다시 로드
            try {
                JSONObject response = ApiClient.get("/reviews/" + review.getId());
                if (response.optBoolean("success", false)) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        review = new Review(data);
                        saltboxLabel.setText("제설함 ID: " + review.getSaltboxId());
                        String ratingStr = repeatString("★", review.getRating()) + repeatString("☆", 5 - review.getRating());
                        ratingLabel.setText("별점: " + ratingStr + " (" + review.getRating() + "/5)");
                        contentArea.setText(review.getContent());
                        dateLabel.setText("작성일: " + review.getCreatedAt());
                        refreshNeeded = true;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "후기를 다시 불러오는데 실패했습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteReview() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "정말 이 후기를 삭제하시겠습니까?",
            "삭제 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                JSONObject data = new JSONObject();
                data.put("user_id", Session.getInstance().getUserId());
                
                JSONObject response = ApiClient.delete("/reviews/" + review.getId(), data);
                
                if (response.optBoolean("success", false)) {
                    JOptionPane.showMessageDialog(this, 
                        "후기가 삭제되었습니다.",
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








