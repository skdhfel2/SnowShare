package components.common;

import core.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * 커뮤니티용 게시판 상단 헤더 공통 컴포넌트
 * - 제목, 설명
 * - 정렬 콤보박스
 * - 기본 액션 버튼(글쓰기/후기 작성 등) + 새로고침 버튼
 * - 하단 요약 라벨
 */
public class BoardHeaderPanel extends JPanel {
    private final JLabel titleLabel;
    private final JLabel descLabel;
    private final JComboBox<String> sortComboBox;
    private final JButton primaryButton;
    private final JButton refreshButton;
    private final JLabel summaryLabel;
    private final JTextField searchField;
    private final JButton searchButton;

    /**
     * @param title            상단 제목 텍스트
     * @param description      제목 아래 설명 텍스트
     * @param primaryButtonText 우측 메인 버튼 텍스트 (예: "글쓰기", "후기 작성")
     * @param sortOptions      정렬 콤보박스 옵션 문자열 배열
     */
    public BoardHeaderPanel(
            String title,
            String description,
            String primaryButtonText,
            String[] sortOptions
    ) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // 좌측: 제목 + 설명
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(BasePanel.FONT_TITLE);

        descLabel = new JLabel(description);
        descLabel.setFont(BasePanel.FONT_BODY);
        descLabel.setForeground(Color.DARK_GRAY);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(descLabel);

        // 우측: 검색 필드 + 정렬 콤보 + 버튼들
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // 검색 필드
        searchField = new JTextField(15);
        searchField.setFont(BasePanel.FONT_BODY);
        searchField.setToolTipText("검색어를 입력하세요");
        searchButton = new JButton("검색");
        searchButton.setFont(BasePanel.FONT_BODY);
        
        sortComboBox = new JComboBox<>(sortOptions);

        primaryButton = new JButton(primaryButtonText);
        refreshButton = new JButton("새로고침");

        rightPanel.add(new JLabel("검색:"));
        rightPanel.add(searchField);
        rightPanel.add(searchButton);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(sortComboBox);
        rightPanel.add(primaryButton);
        rightPanel.add(refreshButton);

        // 하단: 요약 정보
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(BasePanel.FONT_BODY);
        summaryLabel.setForeground(Color.GRAY);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    public JComboBox<String> getSortComboBox() {
        return sortComboBox;
    }

    public JButton getPrimaryButton() {
        return primaryButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JLabel getSummaryLabel() {
        return summaryLabel;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }
}


