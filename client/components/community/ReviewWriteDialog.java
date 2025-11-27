package components.community;

import models.Review;
import utils.ApiClient;
import core.Session;
import org.json.JSONObject;
import utils.SnowBoxStore;
import utils.SnowBoxStore.SnowBoxInfo;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 후기 작성/수정 다이얼로그
 */
public class ReviewWriteDialog extends JDialog {
    // 제설함 선택 관련 컴포넌트
    private JLabel selectedSnowBoxLabel;
    private JButton selectSnowBoxButton;
    private Integer selectedSaltboxId; // 실제로 서버에 전송할 제설함 ID

    private JComboBox<Integer> ratingComboBox;
    private JTextArea contentArea;
    private JButton saveButton;
    private JButton cancelButton;
    private Review review;
    private boolean success = false;
    
    public ReviewWriteDialog(JFrame parent, Review review) {
        super(parent, review == null ? "후기 작성" : "후기 수정", true);
        this.review = review;
        initializeDialog();
    }

    /**
     * 제설함 선택 다이얼로그 열기
     */
    private void openSnowBoxSelectDialog() {
        SnowBoxSelectDialog dialog = new SnowBoxSelectDialog((JFrame) getParent(), selectedSaltboxId);
        dialog.setVisible(true);

        SnowBoxInfo selected = dialog.getSelectedSnowBox();
        if (selected != null) {
            selectedSaltboxId = selected.getId();
            selectedSnowBoxLabel.setText(formatSnowBoxText(selected));
        }
    }

    /**
     * 제설함 정보를 사람이 읽기 좋은 문자열로 변환
     */
    private String formatSnowBoxText(SnowBoxInfo info) {
        String num = info.getSboxNum() == null || info.getSboxNum().isEmpty()
                ? "(번호 없음)" : info.getSboxNum();
        String loc = info.getDetlCn() == null || info.getDetlCn().isEmpty()
                ? "(위치 정보 없음)" : info.getDetlCn();
        String org = info.getMgcNm() == null || info.getMgcNm().isEmpty()
                ? "" : " - " + info.getMgcNm();
        return num + " - " + loc + org;
    }
    
    private void initializeDialog() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // 중앙: 입력 필드
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 제설함 선택 영역
        JLabel saltboxLabel = new JLabel("제설함 위치:");
        saltboxLabel.setFont(core.BasePanel.FONT_BODY);

        selectedSnowBoxLabel = new JLabel("선택된 제설함이 없습니다.");
        selectedSnowBoxLabel.setFont(core.BasePanel.FONT_BODY);

        selectSnowBoxButton = new JButton("제설함 찾기");
        selectSnowBoxButton.setFont(core.BasePanel.FONT_BODY);
        selectSnowBoxButton.addActionListener(e -> openSnowBoxSelectDialog());

        JPanel saltboxInfoPanel = new JPanel(new BorderLayout(5, 5));
        saltboxInfoPanel.add(selectedSnowBoxLabel, BorderLayout.CENTER);
        saltboxInfoPanel.add(selectSnowBoxButton, BorderLayout.EAST);

        JPanel saltboxPanel = new JPanel(new BorderLayout(5, 5));
        saltboxPanel.add(saltboxLabel, BorderLayout.NORTH);
        saltboxPanel.add(saltboxInfoPanel, BorderLayout.CENTER);
        
        // 별점 선택
        JLabel ratingLabel = new JLabel("별점:");
        ratingLabel.setFont(core.BasePanel.FONT_BODY);
        ratingComboBox = new JComboBox<>();
        ratingComboBox.setFont(core.BasePanel.FONT_BODY);
        for (int i = 1; i <= 5; i++) {
            ratingComboBox.addItem(i);
        }
        
        JPanel ratingPanel = new JPanel(new BorderLayout(5, 5));
        ratingPanel.add(ratingLabel, BorderLayout.NORTH);
        ratingPanel.add(ratingComboBox, BorderLayout.CENTER);
        
        // 상단 패널 (제설함 + 별점)
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.add(saltboxPanel);
        topPanel.add(ratingPanel);
        
        // 내용 입력
        JLabel contentLabel = new JLabel("후기 내용:");
        contentLabel.setFont(core.BasePanel.FONT_BODY);
        contentArea = new JTextArea(12, 40);
        contentArea.setFont(core.BasePanel.FONT_BODY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(contentScroll, BorderLayout.CENTER);
        
        inputPanel.add(topPanel, BorderLayout.NORTH);
        inputPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 수정 모드인 경우 기존 데이터 로드
        if (review != null) {
            selectedSaltboxId = review.getSaltboxId();
            SnowBoxInfo info = SnowBoxStore.getById(selectedSaltboxId);
            if (info != null) {
                selectedSnowBoxLabel.setText(formatSnowBoxText(info));
            } else {
                selectedSnowBoxLabel.setText("제설함 ID: " + selectedSaltboxId);
            }
            // 수정 시 제설함 변경 불가
            selectSnowBoxButton.setEnabled(false);
            ratingComboBox.setSelectedItem(review.getRating());
            contentArea.setText(review.getContent());
        }
        
        // 하단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");
        
        saveButton.addActionListener(e -> saveReview());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void saveReview() {
        if (selectedSaltboxId == null || selectedSaltboxId <= 0) {
            JOptionPane.showMessageDialog(this, "제설함을 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer saltboxId = selectedSaltboxId;
        Integer rating = (Integer) ratingComboBox.getSelectedItem();
        String content = contentArea.getText().trim();
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "후기 내용을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
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
            data.put("saltbox_id", saltboxId);
            data.put("user_id", userId);
            data.put("rating", rating);
            data.put("content", content);
            
            JSONObject response;
            if (review == null) {
                // 작성
                response = ApiClient.post("/reviews", data);
            } else {
                // 수정
                response = ApiClient.put("/reviews/" + review.getId(), data);
            }
            
            if (response.optBoolean("success", false)) {
                JOptionPane.showMessageDialog(this, 
                    review == null ? "후기가 작성되었습니다." : "후기가 수정되었습니다.",
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








