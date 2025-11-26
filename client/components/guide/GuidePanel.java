package components.guide;

import core.BasePanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

/**
 * 폭설 대응 안내 화면
 */
public class GuidePanel extends BasePanel {

    // 배경/카드/텍스트 컬러
    private static final Color BG_COLOR = new Color(244, 246, 248);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_TITLE = new Color(33, 37, 41);
    private static final Color RED_ACCENT = new Color(231, 76, 60);
    private static final Color BLUE_ACCENT = new Color(52, 152, 219);
    private static final Color GREEN_ACCENT = new Color(46, 204, 113);

    // 폰트 (맑은 고딕 사용, 시스템에 없으면 기본 폰트로 대체됨)
    private static final Font FONT_HEAD = new Font("Malgun Gothic", Font.BOLD, 28);
    private static final Font FONT_TITLE = new Font("Malgun Gothic", Font.BOLD, 18);
    private static final Font FONT_BODY = new Font("Malgun Gothic", Font.PLAIN, 14);
    private static final Font FONT_CONTACT_MAIN = new Font("Malgun Gothic", Font.BOLD, 22);
    private static final Font FONT_CONTACT_SUB = new Font("Malgun Gothic", Font.PLAIN, 13);

    public GuidePanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_COLOR);
        mainContent.setBorder(new EmptyBorder(40, 40, 40, 40)); // 전체 여백

        // 1. 헤더
        mainContent.add(createHeader());
        mainContent.add(Box.createVerticalStrut(35));

        // 2. [의무] 집 앞 눈 치우기
        mainContent.add(createHorizontalCard(
                "public/images/snow_clearing.png",
                "1. 내 집 앞 눈 치우기 의무",
                "<html><body style='width:320px; line-height:1.6; color:#555555;'>"
                        + "자연재해대책법에 따라 <b>내 집, 내 점포 앞 눈</b>은 직접 치워야 합니다.<br><br>"
                        + "• <b>주간:</b> 눈 그친 후 <font color='#e74c3c'><b>4시간 이내</b></font><br>"
                        + "• <b>야간:</b> 다음날 <font color='#3498db'><b>오전 11시</b></font>까지<br>"
                        + "• <b>범위:</b> 대문 앞 및 보도 (통행로 확보)"
                        + "</body></html>",
                RED_ACCENT
        ));
        mainContent.add(Box.createVerticalStrut(25));

        // 3. [안전] 보행 안전
        mainContent.add(createHorizontalCard(
                "public/images/penguin_walk.png",
                "2. 빙판길 보행 안전 요령",
                "<html><body style='width:320px; line-height:1.6; color:#555555;'>"
                        + "빙판길 낙상 사고 예방을 위해 아래 수칙을 지켜주세요.<br><br>"
                        + "• <b>보행법:</b> 평소보다 보폭을 좁게 천천히 걷기<br>"
                        + "• <b>복장:</b> <font color='#3498db'><b>주머니에서 손 빼기</b></font> (장갑 착용)<br>"
                        + "• <b>신발:</b> 굽이 낮고 미끄럼 방지된 신발"
                        + "</body></html>",
                BLUE_ACCENT
        ));
        mainContent.add(Box.createVerticalStrut(25));

        // 4. [장비] 제설함
        mainContent.add(createHorizontalCard(
                "public/images/grit_bin.png",
                "3. 제설 도구/염화칼슘 확보",
                "<html><body style='width:320px; line-height:1.6; color:#555555;'>"
                        + "급하게 제설 장비가 필요할 때 확인하세요.<br><br>"
                        + "• <b>제설함:</b> 도로변 <font color='#2980b9'><b>파란색 보관함</b></font> (누구나 사용)<br>"
                        + "• <b>무료 대여:</b> 관할 주민센터 (신분증 지참)<br>"
                        + "• <b>위치:</b> 상단 [제설함 지도] 탭 이용"
                        + "</body></html>",
                GREEN_ACCENT
        ));
        mainContent.add(Box.createVerticalStrut(40));

        // 5. [긴급] 비상 연락처
        mainContent.add(createEmergencySection());
        mainContent.add(Box.createVerticalStrut(20));

        // 스크롤 설정
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(scrollPane, BorderLayout.CENTER);
    }

    // 헤더 영역
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("폭설 대응 가이드");
        title.setFont(FONT_HEAD);
        title.setForeground(TEXT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("안전한 겨울을 위한 시민 행동요령");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(Color.GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);

        return panel;
    }

    // 왼쪽 이미지 + 오른쪽 텍스트 카드
    private JPanel createHorizontalCard(String imagePath, String titleText,
                                        String bodyHtml, Color barColor) {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // 1. 왼쪽 이미지
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.decode("#F1F3F5")); // 이미지 없을 때 색상
        imageLabel.setPreferredSize(new Dimension(260, 210));

        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(260, 210, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                imageLabel.setText("No Image");
                imageLabel.setForeground(Color.GRAY);
                imageLabel.setFont(FONT_BODY);
            }
        } catch (Exception e) {
            imageLabel.setText("Error");
            imageLabel.setForeground(Color.RED);
            imageLabel.setFont(FONT_BODY);
        }

        // 2. 오른쪽 텍스트
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(CARD_BG);
        textPanel.setBorder(new EmptyBorder(25, 30, 25, 20));

        // 제목 (좌측 컬러 바)
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_BG);

        JPanel colorBar = new JPanel();
        colorBar.setBackground(barColor);
        colorBar.setPreferredSize(new Dimension(5, 0)); // 컬러 띠 두께

        JLabel title = new JLabel("  " + titleText);
        title.setFont(FONT_TITLE);
        title.setForeground(barColor);

        titlePanel.add(colorBar, BorderLayout.WEST);
        titlePanel.add(title, BorderLayout.CENTER);

        // 본문
        JLabel body = new JLabel(bodyHtml);
        body.setFont(FONT_BODY);
        body.setVerticalAlignment(SwingConstants.TOP);
        body.setBorder(new EmptyBorder(15, 12, 0, 0));

        textPanel.add(titlePanel, BorderLayout.NORTH);
        textPanel.add(body, BorderLayout.CENTER);

        card.add(imageLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    // 긴급 연락처 섹션
    private JPanel createEmergencySection() {
        JPanel panel = new JPanel(new BorderLayout(0, 25));
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("긴급 신고 및 문의");
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 22));
        title.setForeground(TEXT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 0));
        grid.setBackground(BG_COLOR);

        grid.add(createStaticContactBox("110/120", "민원 콜센터"));
        grid.add(createStaticContactBox("119", "재난/구조"));
        grid.add(createStaticContactBox("112", "경찰청"));

        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

    // 개별 연락처 박스
    private JPanel createStaticContactBox(String mainText, String subText) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        box.setPreferredSize(new Dimension(120, 110)); // 높이 키움

        JLabel mainLabel = new JLabel(mainText, SwingConstants.CENTER);
        mainLabel.setFont(FONT_CONTACT_MAIN);
        mainLabel.setForeground(new Color(44, 62, 80));
        mainLabel.setBorder(new EmptyBorder(15, 0, 5, 0));

        JLabel subLabel = new JLabel(subText, SwingConstants.CENTER);
        subLabel.setFont(FONT_CONTACT_SUB);
        subLabel.setForeground(Color.GRAY);
        subLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        box.add(mainLabel, BorderLayout.CENTER);
        box.add(subLabel, BorderLayout.SOUTH);

        return box;
    }
}

