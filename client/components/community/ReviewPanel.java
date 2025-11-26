package components.community;

import core.BasePanel;
import models.Review;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.ApiClient;
import utils.SnowBoxStore;
import utils.SnowBoxStore.SnowBoxInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 후기게시판 패널
 * - 제설함 후기를 목록으로 보여주고
 * - 후기 작성 / 새로고침 / 더블클릭 상세보기 기능 제공
 */
public class ReviewPanel extends BasePanel {
    private JTable reviewTable;
    private DefaultTableModel tableModel;
    private JButton writeButton;
    private JButton refreshButton;
    private List<Review> reviews;

    private static final String[] COLUMN_NAMES = {"번호", "제설함", "별점", "작성자", "작성일"};

    public ReviewPanel() {
        reviews = new ArrayList<>();
        initializePanel();
        loadReviews();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // 상단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        writeButton = new JButton("후기 작성");
        refreshButton = new JButton("새로고침");

        writeButton.addActionListener(e -> openWriteDialog());
        refreshButton.addActionListener(e -> loadReviews());

        buttonPanel.add(writeButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);

        // 중앙: 후기 목록 테이블
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reviewTable = new JTable(tableModel);
        reviewTable.setRowHeight(30);
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reviewTable.getTableHeader().setReorderingAllowed(false);

        // 컬럼 너비 설정
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // 번호
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(260);  // 제설함
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // 별점
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // 작성자
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // 작성일

        // 더블클릭 시 상세보기
        reviewTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = reviewTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < reviews.size()) {
                        openDetailDialog(reviews.get(selectedRow));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reviewTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 후기 목록 로드
     */
    private void loadReviews() {
        SwingUtilities.invokeLater(() -> {
            try {
                JSONObject response = ApiClient.get("/reviews");
                if (response.optBoolean("success", false)) {
                    JSONArray data = response.optJSONArray("data");
                    reviews.clear();
                    tableModel.setRowCount(0);

                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            Review review = new Review(data.getJSONObject(i));
                            reviews.add(review);

                            String saltboxText = formatSaltbox(review.getSaltboxId());
                            String ratingText = formatRating(review.getRating());

                            String author = review.getUsername().isEmpty()
                                    ? review.getUserId()
                                    : review.getUsername();

                            String dateStr = review.getCreatedAt();
                            if (dateStr != null && dateStr.length() > 16) {
                                dateStr = dateStr.substring(0, 16);
                            }

                            tableModel.addRow(new Object[]{
                                    review.getId(),
                                    saltboxText,
                                    ratingText,
                                    author,
                                    dateStr
                            });
                        }
                    }
                } else {
                    showError("후기 목록을 불러오는데 실패했습니다.");
                }
            } catch (IOException e) {
                showError("서버 연결에 실패했습니다: " + e.getMessage());
            }
        });
    }

    /**
     * 후기 작성 다이얼로그 열기
     */
    private void openWriteDialog() {
        ReviewWriteDialog dialog = new ReviewWriteDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadReviews();
        }
    }

    /**
     * 상세보기 다이얼로그 열기
     */
    private void openDetailDialog(Review review) {
        ReviewDetailDialog dialog = new ReviewDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), review);
        dialog.setVisible(true);
        if (dialog.isRefreshNeeded()) {
            loadReviews();
        }
    }

    /**
     * 제설함 ID를 사람이 읽기 좋은 텍스트로 변환
     */
    private String formatSaltbox(int saltboxId) {
        SnowBoxInfo info = SnowBoxStore.getById(saltboxId);
        if (info == null) {
            return "제설함 ID: " + saltboxId;
        }
        String num = safe(info.getSboxNum());
        String loc = safe(info.getDetlCn());
        String org = safe(info.getMgcNm());
        return String.format("%s - %s (%s)", num, loc, org);
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    /**
     * 숫자 별점을 "★★★★☆ (4/5)" 형식의 문자열로 변환
     */
    private String formatRating(int rating) {
        int r = Math.max(1, Math.min(5, rating));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r; i++) {
            sb.append("★");
        }
        for (int i = r; i < 5; i++) {
            sb.append("☆");
        }
        sb.append(" (").append(r).append("/5)");
        return sb.toString();
    }
}


