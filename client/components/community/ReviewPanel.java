package components.community;

import core.BasePanel;
import components.common.BoardHeaderPanel;
import models.Review;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.ApiClient;
import utils.SnowBoxStore;
import utils.SnowBoxStore.SnowBoxInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import javax.swing.SwingConstants;
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
    private JComboBox<String> sortComboBox;
    private JLabel summaryLabel;
    private JTextField searchField;
    private JButton searchButton;
    private List<Review> reviews;

    private static final String[] COLUMN_NAMES = {"제설함", "별점", "댓글", "조회수", "작성자", "작성일"};

    public ReviewPanel() {
        reviews = new ArrayList<>();
        initializePanel();
        loadReviews();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // 상단: 공통 헤더 컴포넌트 사용
        BoardHeaderPanel headerPanel = new BoardHeaderPanel(
                "후기게시판",
                "제설함 이용 후기를 공유하는 공간입니다.",
                "후기 작성",
                new String[]{"정렬: 최신순", "정렬: 별점 높은순", "정렬: 조회수순", "정렬: 댓글 많은순"}
        );
        sortComboBox = headerPanel.getSortComboBox();
        writeButton = headerPanel.getPrimaryButton();
        refreshButton = headerPanel.getRefreshButton();
        summaryLabel = headerPanel.getSummaryLabel();
        searchField = headerPanel.getSearchField();
        searchButton = headerPanel.getSearchButton();

        writeButton.addActionListener(e -> openWriteDialog());
        refreshButton.addActionListener(e -> loadReviews());
        sortComboBox.addActionListener(e -> refreshTable());
        searchButton.addActionListener(e -> refreshTable());
        searchField.addActionListener(e -> refreshTable()); // Enter 키로도 검색 가능

        add(headerPanel, BorderLayout.NORTH);

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

        // 모든 컬럼 가운데 정렬 (데이터 셀) - 배경색도 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(0xE3F2FD)); // 선택된 행: 연한 파란색
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE); // 일반 행: 흰색
                    c.setForeground(Color.BLACK);
                }
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < reviewTable.getColumnModel().getColumnCount(); i++) {
            reviewTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // 컬럼 헤더도 가운데 정렬 + 배경색 설정
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(0xF5F5F5)); // 헤더: 연한 회색 배경
                c.setForeground(new Color(0x424242)); // 헤더: 진한 회색 텍스트
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel) c).setFont(c.getFont().deriveFont(Font.BOLD)); // 헤더는 굵게
                return c;
            }
        };
        reviewTable.getTableHeader().setDefaultRenderer(headerRenderer);

        // 컬럼 너비 설정 (번호 컬럼 제거 후 재조정)
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(260);  // 제설함
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(120);  // 별점
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(70);   // 댓글
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(70);   // 조회수
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // 작성자
        reviewTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // 작성일

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

                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            Review review = new Review(data.getJSONObject(i));
                            reviews.add(review);
                        }
                    }

                    // 정렬/요약을 포함해 테이블 갱신
                    refreshTable();
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
        // 상세보기 전에 최신 데이터 및 조회수 반영을 위해 서버에서 다시 조회
        Review latestReview = review;
        try {
            JSONObject response = ApiClient.get("/reviews/" + review.getId());
            if (response.optBoolean("success", false)) {
                JSONObject data = response.optJSONObject("data");
                if (data != null) {
                    latestReview = new Review(data);
                }
            }
        } catch (IOException e) {
            // 실패해도 기존 review 객체로 상세보기는 계속 진행
        }

        ReviewDetailDialog dialog = new ReviewDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                latestReview
        );
        dialog.setVisible(true);

        // 상세 보기 시 조회수가 증가하므로, 다이얼로그 종료 후 목록을 새로고침
        loadReviews();
    }

    /**
     * 정렬 기준에 따라 테이블을 갱신하고 요약 정보를 업데이트
     */
    private void refreshTable() {
        if (reviews == null) return;

        // 검색어로 필터링 (제설함 위치 검색)
        String searchKeyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Review> filtered = new ArrayList<>();
        if (searchKeyword.isEmpty()) {
            filtered.addAll(reviews);
        } else {
            for (Review review : reviews) {
                SnowBoxInfo info = SnowBoxStore.getById(review.getSaltboxId());
                if (info != null) {
                    String sboxNum = info.getSboxNum() != null ? info.getSboxNum().toLowerCase() : "";
                    String detlCn = info.getDetlCn() != null ? info.getDetlCn().toLowerCase() : "";
                    String mgcNm = info.getMgcNm() != null ? info.getMgcNm().toLowerCase() : "";
                    if (sboxNum.contains(searchKeyword) || detlCn.contains(searchKeyword) || mgcNm.contains(searchKeyword)) {
                        filtered.add(review);
                    }
                }
            }
        }

        List<Review> sorted = new ArrayList<>(filtered);
        int sortIndex = sortComboBox != null ? sortComboBox.getSelectedIndex() : 0;

        switch (sortIndex) {
            case 1: // 별점 높은순
                sorted.sort((a, b) -> Integer.compare(b.getRating(), a.getRating()));
                break;
            case 2: // 조회수순
                sorted.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
                break;
            case 3: // 댓글 많은순
                sorted.sort((a, b) -> Integer.compare(b.getCommentCount(), a.getCommentCount()));
                break;
            default: // 최신순 (created_at DESC 순서 유지)
                // 그대로 사용
                break;
        }

        tableModel.setRowCount(0);
        for (Review review : sorted) {
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
                    saltboxText,
                    ratingText,
                    review.getCommentCount(),
                    review.getViewCount(),
                    author,
                    dateStr
            });
        }

        // 요약 정보 업데이트 (검색 결과 반영)
        int count = sorted.size();
        int sumRating = 0;
        for (Review r : sorted) {
            sumRating += r.getRating();
        }
        double avgRating = count > 0 ? (double) sumRating / count : 0.0;
        String searchText = searchKeyword.isEmpty() ? "" : " (검색: \"" + searchField.getText().trim() + "\")";
        summaryLabel.setText(
                String.format("총 %d개의 후기가 있습니다. 평균 별점: %.1f / 5%s", count, avgRating, searchText)
        );
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


