package components.community;

import utils.SnowBoxStore;
import utils.SnowBoxStore.SnowBoxInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 제설함 위치를 검색해서 선택할 수 있는 다이얼로그.
 * 후기 작성 시 제설함을 선택하는 용도로 사용한다.
 */
public class SnowBoxSelectDialog extends JDialog {
    private JTextField searchField;
    private JList<SnowBoxInfo> snowBoxList;
    private DefaultListModel<SnowBoxInfo> listModel;
    private JButton selectButton;
    private JButton cancelButton;

    private SnowBoxInfo selectedSnowBox;

    public SnowBoxSelectDialog(JFrame parent, Integer initialId) {
        super(parent, "제설함 선택", true);
        initializeDialog(initialId);
    }

    private void initializeDialog(Integer initialId) {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // 상단 검색 필드
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JLabel searchLabel = new JLabel("검색 (번호 / 위치 / 관리기관):");
        searchField = new JTextField();

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // 입력이 바뀔 때마다 필터링
                loadList(searchField.getText());
            }
        });

        topPanel.add(searchLabel, BorderLayout.NORTH);
        topPanel.add(searchField, BorderLayout.CENTER);

        // 중앙 리스트
        listModel = new DefaultListModel<>();
        snowBoxList = new JList<>(listModel);
        snowBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 간단한 렌더러: SnowBoxInfo.toString() 사용
        snowBoxList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SnowBoxInfo) {
                    SnowBoxInfo info = (SnowBoxInfo) value;
                    String text = String.format("%s - %s (%s)",
                            nullToEmpty(info.getSboxNum()),
                            nullToEmpty(info.getDetlCn()),
                            nullToEmpty(info.getMgcNm()));
                    setText(text);
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(snowBoxList);

        // 하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        selectButton = new JButton("선택");
        cancelButton = new JButton("취소");

        selectButton.addActionListener(e -> {
            SnowBoxInfo selected = snowBoxList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "제설함을 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedSnowBox = selected;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            selectedSnowBox = null;
            dispose();
        });

        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 전체 목록 로드
        loadList("");

        // 초기 선택 ID가 있으면 해당 ID 항목 선택
        if (initialId != null && initialId > 0) {
            SnowBoxInfo info = SnowBoxStore.getById(initialId);
            if (info != null) {
                snowBoxList.setSelectedValue(info, true);
            }
        }
    }

    private void loadList(String keyword) {
        List<SnowBoxInfo> list = SnowBoxStore.search(keyword);
        listModel.clear();
        for (SnowBoxInfo info : list) {
            listModel.addElement(info);
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * 다이얼로그에서 선택된 제설함 정보를 반환
     */
    public SnowBoxInfo getSelectedSnowBox() {
        return selectedSnowBox;
    }
}


