package components.community;

import core.BasePanel;
import models.Post;
import utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private List<Post> posts;
    
    private static final String[] COLUMN_NAMES = {"번호", "제목", "작성자", "작성일"};
    
    public FreeBoardPanel() {
        posts = new ArrayList<>();
        initializePanel();
        loadPosts();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        
        // 상단: 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        writeButton = new JButton("글쓰기");
        refreshButton = new JButton("새로고침");
        
        writeButton.addActionListener(e -> openWriteDialog());
        refreshButton.addActionListener(e -> loadPosts());
        
        buttonPanel.add(writeButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);
        
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
        
        // 컬럼 너비 설정
        postTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // 번호
        postTable.getColumnModel().getColumn(1).setPreferredWidth(400);  // 제목
        postTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // 작성자
        postTable.getColumnModel().getColumn(3).setPreferredWidth(150); // 작성일
        
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
                    tableModel.setRowCount(0);
                    
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
                            
                            tableModel.addRow(new Object[]{
                                post.getId(),
                                post.getTitle(),
                                post.getUsername().isEmpty() ? post.getUserId() : post.getUsername(),
                                dateStr
                            });
                        }
                    }
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
        PostDetailDialog dialog = new PostDetailDialog((JFrame) SwingUtilities.getWindowAncestor(this), post);
        dialog.setVisible(true);
        if (dialog.isRefreshNeeded()) {
            loadPosts();
        }
    }
}


