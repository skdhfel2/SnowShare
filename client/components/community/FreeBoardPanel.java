package components.community;

import core.BasePanel;
import components.common.BoardHeaderPanel;
import models.Post;
import utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 자유게시판 패널
 */
public class FreeBoardPanel extends BasePanel {
    private JTable postTable;
    private DefaultTableModel tableModel;
    private JButton writeButton;
    private JButton refreshButton;
    private JComboBox<String> sortComboBox;
    private JLabel summaryLabel;
    private JTextField searchField;
    private JButton searchButton;
    private List<Post> posts;
    
    private static final String[] COLUMN_NAMES = {"제목", "작성자", "댓글", "조회수", "작성일"};
    
    public FreeBoardPanel() {
        posts = new ArrayList<>();
        initializePanel();
        loadPosts();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        
        // 상단: 공통 헤더 컴포넌트 사용
        BoardHeaderPanel headerPanel = new BoardHeaderPanel(
                "자유게시판",
                "제설과 관련된 다양한 이야기를 자유롭게 나누세요.",
                "글쓰기",
                new String[]{"정렬: 최신순", "정렬: 조회수순", "정렬: 댓글 많은순"}
        );
        sortComboBox = headerPanel.getSortComboBox();
        writeButton = headerPanel.getPrimaryButton();
        refreshButton = headerPanel.getRefreshButton();
        summaryLabel = headerPanel.getSummaryLabel();
        searchField = headerPanel.getSearchField();
        searchButton = headerPanel.getSearchButton();

        writeButton.addActionListener(e -> openWriteDialog());
        refreshButton.addActionListener(e -> loadPosts());
        sortComboBox.addActionListener(e -> refreshTable());
        searchButton.addActionListener(e -> refreshTable());
        searchField.addActionListener(e -> refreshTable()); // Enter 키로도 검색 가능

        add(headerPanel, BorderLayout.NORTH);
        
        // 중앙: 게시글 목록 테이블
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        postTable = new JTable(tableModel);
        postTable.setRowHeight(30);
        postTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postTable.getTableHeader().setReorderingAllowed(false);

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
        for (int i = 0; i < postTable.getColumnModel().getColumnCount(); i++) {
            postTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
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
        postTable.getTableHeader().setDefaultRenderer(headerRenderer);

        // 컬럼 너비 설정 (번호 컬럼 제거 후 재조정)
        postTable.getColumnModel().getColumn(0).setPreferredWidth(360);  // 제목
        postTable.getColumnModel().getColumn(1).setPreferredWidth(120);  // 작성자
        postTable.getColumnModel().getColumn(2).setPreferredWidth(70);   // 댓글
        postTable.getColumnModel().getColumn(3).setPreferredWidth(70);   // 조회수
        postTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // 작성일
        
        // 더블클릭 시 상세보기
        postTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = postTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < posts.size()) {
                        openDetailDialog(posts.get(selectedRow));
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(postTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 게시글 목록 로드
     */
    private void loadPosts() {
        SwingUtilities.invokeLater(() -> {
            try {
                JSONObject response = ApiClient.get("/posts");
                if (response.optBoolean("success", false)) {
                    JSONArray data = response.optJSONArray("data");
                    posts.clear();
                    
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            Post post = new Post(data.getJSONObject(i));
                            posts.add(post);
                            
                            // 테이블에 추가
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            String dateStr = post.getCreatedAt();
                            try {
                                if (dateStr != null && !dateStr.isEmpty()) {
                                    // MySQL TIMESTAMP 형식 파싱
                                    dateStr = dateStr.substring(0, Math.min(16, dateStr.length()));
                                }
                            } catch (Exception e) {
                                dateStr = post.getCreatedAt();
                            }
                        }
                    }

                    refreshTable();
                } else {
                    showError("게시글 목록을 불러오는데 실패했습니다.");
                }
            } catch (IOException e) {
                showError("서버 연결에 실패했습니다: " + e.getMessage());
            }
        });
    }
    
    /**
     * 글쓰기 다이얼로그 열기
     */
    private void openWriteDialog() {
        PostWriteDialog dialog = new PostWriteDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadPosts();
        }
    }
    
    /**
     * 상세보기 다이얼로그 열기
     */
    private void openDetailDialog(Post post) {
        // 상세보기 전에 최신 데이터 및 조회수 반영을 위해 서버에서 다시 조회
        Post latestPost = post;
        try {
            JSONObject response = ApiClient.get("/posts/" + post.getId());
            if (response.optBoolean("success", false)) {
                JSONObject data = response.optJSONObject("data");
                if (data != null) {
                    latestPost = new Post(data);
                }
            }
        } catch (IOException e) {
            // 실패해도 기존 post 객체로 상세보기는 계속 진행
        }

        PostDetailDialog dialog = new PostDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                latestPost
        );
        dialog.setVisible(true);

        // 상세 보기 시 조회수가 증가하므로, 다이얼로그 종료 후 목록을 새로고침
        loadPosts();
    }

    /**
     * 정렬 기준에 따라 테이블을 갱신하고 요약 정보를 업데이트
     */
    private void refreshTable() {
        if (posts == null) return;

        // 검색어로 필터링
        String searchKeyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Post> filtered = new ArrayList<>();
        if (searchKeyword.isEmpty()) {
            filtered.addAll(posts);
        } else {
            for (Post post : posts) {
                if (post.getTitle().toLowerCase().contains(searchKeyword)) {
                    filtered.add(post);
                }
            }
        }

        List<Post> sorted = new ArrayList<>(filtered);
        int sortIndex = sortComboBox != null ? sortComboBox.getSelectedIndex() : 0;

        switch (sortIndex) {
            case 1: // 조회수순
                sorted.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
                break;
            case 2: // 댓글 많은순
                sorted.sort((a, b) -> Integer.compare(b.getCommentCount(), a.getCommentCount()));
                break;
            default: // 최신순 (created_at DESC 순서 유지)
                // 그대로 사용
                break;
        }

        tableModel.setRowCount(0);
        for (Post post : sorted) {
            String dateStr = post.getCreatedAt();
            if (dateStr != null && !dateStr.isEmpty() && dateStr.length() > 16) {
                dateStr = dateStr.substring(0, 16);
            }

            tableModel.addRow(new Object[]{
                    post.getTitle(),
                    post.getUsername().isEmpty() ? post.getUserId() : post.getUsername(),
                    post.getCommentCount(),
                    post.getViewCount(),
                    dateStr
            });
        }

        // 요약 정보 업데이트 (검색 결과 반영)
        int count = sorted.size();
        int sumViews = 0;
        int sumComments = 0;
        for (Post p : sorted) {
            sumViews += p.getViewCount();
            sumComments += p.getCommentCount();
        }
        String searchText = searchKeyword.isEmpty() ? "" : " (검색: \"" + searchField.getText().trim() + "\")";
        summaryLabel.setText(
                String.format("총 %d개의 글이 있습니다. 전체 조회수: %d, 전체 댓글 수: %d%s", count, sumViews, sumComments, searchText)
        );
    }
}


