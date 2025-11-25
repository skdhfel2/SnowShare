package components.news;

import core.BasePanel;
import models.News;
import utils.NewsService;
import utils.AISummarizer;
import utils.IconFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.SimpleDateFormat;

/**
 * 관련뉴스 화면
 */
public class NewsPanel extends BasePanel {
    private JList<News> newsList;
    private DefaultListModel<News> newsListModel;
    private JEditorPane newsDetailEditor;
    private JLabel newsStatusLabel;
    private JButton aiSummaryButton;
    private News currentSelectedNews;
    
    private NewsService newsService;
    private AISummarizer aiSummarizer;
    
    public NewsPanel() {
        newsService = new NewsService();
        aiSummarizer = new AISummarizer();
        initializeComponents();
        setupLayout();
        // 처음 진입 시 자동으로 뉴스 로드
        loadNews();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 뉴스 리스트 초기화
        newsListModel = new DefaultListModel<>();
        newsList = new JList<>(newsListModel);
        currentSelectedNews = null;
        newsList.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        newsList.setSelectionBackground(new Color(135, 206, 250));
        newsList.setSelectionForeground(Color.WHITE);
        newsList.setCellRenderer(new NewsListCellRenderer());
        
        // 뉴스 리스트 선택 이벤트
        newsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                News selected = newsList.getSelectedValue();
                if (selected != null) {
                    currentSelectedNews = selected;
                    updateNewsDetail(selected);
                    aiSummaryButton.setEnabled(true);
                }
            }
        });
        
        // 뉴스 더블클릭 시 브라우저에서 열기
        newsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    News selected = newsList.getSelectedValue();
                    if (selected != null) {
                        openNewsInBrowser(selected.link);
                    }
                }
            }
        });
    }
    
    private void setupLayout() {
        // 상단: 새로고침 버튼 및 AI 요약 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setOpaque(false);
        
        JButton refreshButton = createStyledButton("뉴스 새로고침", new Color(70, 130, 180), IconFactory.createRefreshIcon());
        refreshButton.addActionListener(e -> loadNews());
        
        aiSummaryButton = createStyledButton("AI 요약", new Color(147, 112, 219), IconFactory.createAIIcon());
        aiSummaryButton.addActionListener(e -> generateAISummary());
        aiSummaryButton.setEnabled(false);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(aiSummaryButton);
        
        newsStatusLabel = new JLabel("뉴스를 불러오는 중...");
        newsStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        newsStatusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        newsStatusLabel.setForeground(new Color(100, 100, 100));
        
        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(newsStatusLabel, BorderLayout.CENTER);
        
        // 중앙: 뉴스 리스트와 상세 정보
        JSplitPane newsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        newsSplitPane.setLeftComponent(new JScrollPane(newsList));
        
        // 뉴스 상세 영역
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(new Color(250, 252, 255));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        newsDetailEditor = new JEditorPane("text/html", "");
        newsDetailEditor.setEditable(false);
        newsDetailEditor.setBackground(new Color(250, 252, 255));
        newsDetailEditor.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        newsDetailEditor.setCaretPosition(0);
        
        newsDetailEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && currentSelectedNews != null) {
                    openNewsInBrowser(currentSelectedNews.link);
                }
            }
        });
        
        JScrollPane detailScrollPane = new JScrollPane(newsDetailEditor);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(new Color(250, 252, 255));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        
        newsSplitPane.setRightComponent(detailPanel);
        newsSplitPane.setDividerLocation(400);
        newsSplitPane.setResizeWeight(0.5);
        
        add(topPanel, BorderLayout.NORTH);
        add(newsSplitPane, BorderLayout.CENTER);
    }
    
    private JButton createStyledButton(String text, Color bgColor, ImageIcon icon) {
        JButton button = new JButton(text, icon);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(140, 35));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(8);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
                button.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.setForeground(Color.BLACK);
            }
        });
        
        return button;
    }
    
    private void loadNews() {
        newsStatusLabel.setText("뉴스를 불러오는 중...");
        newsListModel.clear();
        
        new Thread(() -> {
            try {
                java.util.List<News> newsItems = newsService.fetchNews("폭설");
                
                SwingUtilities.invokeLater(() -> {
                    for (News item : newsItems) {
                        newsListModel.addElement(item);
                    }
                    
                    if (newsItems.isEmpty()) {
                        newsStatusLabel.setText("뉴스를 찾을 수 없습니다.");
                    } else {
                        newsStatusLabel.setText(String.format("총 %d개의 뉴스를 불러왔습니다.", newsItems.size()));
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    newsStatusLabel.setText("뉴스 로드 중 오류가 발생했습니다: " + e.getMessage());
                    showError("뉴스를 불러오는 중 오류가 발생했습니다:\n" + e.getMessage());
                });
            }
        }).start();
    }
    
    private void updateNewsDetail(News news) {
        updateNewsDetail(news, null);
    }
    
    private void updateNewsDetail(News news, String aiSummary) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: 맑은 고딕; padding: 10px; background-color: #FAFCFF;'>");
        
        // 제목 섹션
        html.append("<div style='background: linear-gradient(135deg, #4682B4 0%, #87CEEA 100%); padding: 15px; border-radius: 8px; margin-bottom: 15px;'>");
        html.append("<h2 style='color: white; margin: 0; font-size: 18px; font-weight: bold;'>제목</h2>");
        html.append("</div>");
        html.append("<div style='padding: 10px 15px; background-color: white; border-left: 4px solid #4682B4; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
        html.append("<p style='color: #000000; font-size: 15px; line-height: 1.6; margin: 0; font-weight: 500;'>");
        html.append(escapeHtml(cleanHtmlText(news.title)));
        html.append("</p></div>");
        
        // 발행일 섹션
        html.append("<div style='background-color: #F0F8FF; padding: 12px 15px; border-radius: 6px; margin-bottom: 15px; border-left: 4px solid #87CEEA;'>");
        html.append("<span style='color: #000000; font-weight: bold; font-size: 13px;'>발행일: </span>");
        html.append("<span style='color: #000000; font-size: 13px;'>").append(dateFormat.format(news.pubDate)).append("</span>");
        html.append("</div>");
        
        // AI 요약 섹션
        if (aiSummary != null && !aiSummary.trim().isEmpty()) {
            html.append("<div style='background: linear-gradient(135deg, #9370DB 0%, #BA55D3 100%); padding: 15px; border-radius: 8px; margin-bottom: 15px;'>");
            html.append("<h3 style='color: white; margin: 0 0 10px 0; font-size: 16px; font-weight: bold;'>AI 요약</h3>");
            html.append("</div>");
            html.append("<div style='padding: 15px; background-color: #F5F0FF; border-left: 4px solid #9370DB; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);'>");
            html.append("<p style='color: #000000; font-size: 14px; line-height: 1.8; margin: 0;'>");
            html.append(escapeHtml(aiSummary));
            html.append("</p></div>");
        }
        
        // 상세 설명 섹션
        if (news.description != null && !news.description.isEmpty()) {
            String cleanDescription = cleanHtmlText(news.description);
            if (!cleanDescription.trim().isEmpty()) {
                html.append("<div style='background-color: #E8F4F8; padding: 12px 15px; border-radius: 6px; margin-bottom: 15px; border-left: 4px solid #4682B4;'>");
                html.append("<span style='color: #000000; font-weight: bold; font-size: 13px;'>상세 설명</span>");
                html.append("</div>");
                html.append("<div style='padding: 12px 15px; background-color: white; border-radius: 4px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);'>");
                html.append("<p style='color: #000000; font-size: 14px; line-height: 1.7; margin: 0;'>");
                html.append(escapeHtml(cleanDescription));
                html.append("</p></div>");
            }
        }
        
        // 링크 섹션
        html.append("<div style='background-color: #FFF8E1; padding: 12px 15px; border-radius: 6px; margin-bottom: 10px; border-left: 4px solid #FFA500;'>");
        html.append("<span style='color: #000000; font-weight: bold; font-size: 13px;'>원문 링크</span>");
        html.append("</div>");
        html.append("<div style='padding: 10px 15px; background-color: #F5F5F5; border-radius: 4px; margin-bottom: 15px; word-break: break-all;'>");
        html.append("<p style='color: #000000; font-size: 12px; margin: 0; font-family: monospace;'>");
        html.append(escapeHtml(news.link));
        html.append("</p></div>");
        
        html.append("<div style='text-align: center; padding: 10px; background-color: #E3F2FD; border-radius: 6px; margin-top: 15px;'>");
        html.append("<p style='color: #000000; font-size: 12px; margin: 0; font-style: italic;'>");
        html.append("더블클릭하면 브라우저에서 원문을 볼 수 있습니다");
        html.append("</p></div>");
        
        html.append("</body></html>");
        
        newsDetailEditor.setText(html.toString());
        newsDetailEditor.setCaretPosition(0);
    }
    
    private void generateAISummary() {
        if (currentSelectedNews == null) {
            showInfo("뉴스를 먼저 선택해주세요.");
            return;
        }
        
        aiSummaryButton.setEnabled(false);
        aiSummaryButton.setText("AI 요약 중...");
        newsStatusLabel.setText("AI가 기사를 요약하는 중입니다...");
        
        new Thread(() -> {
            try {
                String articleText = cleanHtmlText(currentSelectedNews.description);
                if (articleText == null || articleText.trim().isEmpty()) {
                    articleText = currentSelectedNews.title;
                }
                
                // 텍스트가 너무 길면 잘라내기 (Gemini API 제한)
                if (articleText.length() > 8000) {
                    articleText = articleText.substring(0, 8000) + "...";
                }
                
                String summary = aiSummarizer.summarize(articleText);
                
                SwingUtilities.invokeLater(() -> {
                    updateNewsDetail(currentSelectedNews, summary);
                    aiSummaryButton.setEnabled(true);
                    aiSummaryButton.setText("AI 요약");
                    newsStatusLabel.setText("AI 요약이 완료되었습니다.");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    String errorMessage = e.getMessage();
                    // 에러 메시지가 너무 길면 간단하게 표시
                    if (errorMessage != null && errorMessage.length() > 200) {
                        errorMessage = errorMessage.substring(0, 200) + "...";
                    }
                    
                    showError("AI 요약 생성 중 오류가 발생했습니다:\n\n" + errorMessage + 
                             "\n\n자세한 내용은 콘솔을 확인하세요.");
                    
                    // 콘솔에도 출력
                    System.err.println("AI 요약 오류: " + e.getMessage());
                    e.printStackTrace();
                    
                    aiSummaryButton.setEnabled(true);
                    aiSummaryButton.setText("AI 요약");
                    newsStatusLabel.setText("AI 요약 실패: " + (errorMessage.length() > 50 ? errorMessage.substring(0, 50) + "..." : errorMessage));
                });
            }
        }).start();
    }
    
    private void openNewsInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                newsStatusLabel.setText("브라우저에서 뉴스를 열었습니다: " + url);
            } else {
                showInfo("브라우저를 열 수 없습니다.\n링크: " + url);
            }
        } catch (Exception e) {
            showError("브라우저를 열 수 없습니다: " + e.getMessage() + "\n링크: " + url);
        }
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String cleanHtmlText(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        String text = html;
        text = text.replace("&nbsp;", " ");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&#39;", "'");
        text = text.replace("&apos;", "'");
        text = text.replaceAll("<[^>]+>", "");
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\n\\s*\\n", "\n\n");
        text = text.trim();
        
        return text;
    }
    
    /**
     * 뉴스 리스트 셀 렌더러
     */
    private class NewsListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof News) {
                News news = (News) value;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                if (isSelected) {
                    setBackground(new Color(135, 206, 250));
                    setForeground(Color.WHITE);
                    setText(String.format("<html><b>%s</b><br/><font size='-1'>%s</font></html>",
                            news.title, dateFormat.format(news.pubDate)));
                } else {
                    setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    setForeground(new Color(50, 50, 50));
                    setText(String.format("<html><b>%s</b><br/><font size='-1' color='gray'>%s</font></html>",
                            news.title, dateFormat.format(news.pubDate)));
                }
            }
            return this;
        }
    }
}
