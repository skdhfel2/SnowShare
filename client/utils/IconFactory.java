package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 아이콘 생성 팩토리 클래스
 * Windows Java 환경에서 이모지가 제대로 표시되지 않는 문제를 해결하기 위해
 * 코드로 아이콘을 생성하여 제공합니다.
 */
public class IconFactory {
    
    /**
     * 새로고침/뉴스 아이콘 생성 (신문 모양)
     * @return 새로고침 아이콘
     */
    public static ImageIcon createRefreshIcon() {
        int size = 18;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 신문/문서 모양 그리기
        int x = 3;
        int y = 3;
        int width = size - 6;
        int height = size - 6;
        
        // 신문 본체 (사각형)
        g2d.drawRect(x, y, width, height);
        
        // 신문 접힌 부분 (우측 상단)
        int foldSize = 4;
        g2d.drawLine(x + width - foldSize, y, x + width, y + foldSize);
        g2d.drawLine(x + width, y + foldSize, x + width - foldSize, y + foldSize);
        
        // 신문 텍스트 라인들 (수평선)
        g2d.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i < 3; i++) {
            int lineY = y + 6 + i * 2;
            g2d.drawLine(x + 2, lineY, x + width - 4, lineY);
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * AI 요약 아이콘 생성 (Google Gemini 스타일 - 다이아몬드/별 모양)
     * @return AI 요약 아이콘
     */
    public static ImageIcon createAIIcon() {
        int size = 18;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = size / 2;
        int centerY = size / 2;
        int radius = size / 2 - 2;
        
        // Gemini 스타일 다이아몬드/별 모양 그리기
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 외곽 다이아몬드 (4개 점)
        int[] outerX = {centerX, centerX + radius, centerX, centerX - radius};
        int[] outerY = {centerY - radius, centerY, centerY + radius, centerY};
        
        // 다이아몬드 외곽선
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            g2d.drawLine(outerX[i], outerY[i], outerX[next], outerY[next]);
        }
        
        // 내부 작은 다이아몬드 (45도 회전)
        int innerRadius = radius / 2;
        double angle45 = Math.PI / 4;
        int[] innerX = {
            centerX + (int)(innerRadius * Math.cos(angle45)),
            centerX - (int)(innerRadius * Math.sin(angle45)),
            centerX - (int)(innerRadius * Math.cos(angle45)),
            centerX + (int)(innerRadius * Math.sin(angle45))
        };
        int[] innerY = {
            centerY - (int)(innerRadius * Math.sin(angle45)),
            centerY - (int)(innerRadius * Math.cos(angle45)),
            centerY + (int)(innerRadius * Math.sin(angle45)),
            centerY + (int)(innerRadius * Math.cos(angle45))
        };
        
        // 내부 다이아몬드 그리기
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            g2d.drawLine(innerX[i], innerY[i], innerX[next], innerY[next]);
        }
        
        // 중앙 점
        g2d.fillOval(centerX - 1, centerY - 1, 2, 2);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * 눈송이 아이콘 생성
     * @return 눈송이 아이콘
     */
    public static ImageIcon createSnowflakeIcon() {
        int size = 24;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = size / 2;
        int centerY = size / 2;
        int radius = size / 2 - 2;
        
        // 흰색 눈송이 그리기
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 6방향 대칭 구조로 눈송이 그리기
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2 * i / 6;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            
            // 주 가지 (중앙에서 바깥으로)
            int x1 = centerX + (int)(radius * 0.3 * cos);
            int y1 = centerY - (int)(radius * 0.3 * sin);
            int x2 = centerX + (int)(radius * cos);
            int y2 = centerY - (int)(radius * sin);
            g2d.drawLine(x1, y1, x2, y2);
            
            // 측면 가지 (주 가지 중간에서)
            double sideAngle1 = angle + Math.PI / 3;
            double sideAngle2 = angle - Math.PI / 3;
            int midX = centerX + (int)(radius * 0.6 * cos);
            int midY = centerY - (int)(radius * 0.6 * sin);
            int sideX1 = midX + (int)(radius * 0.3 * Math.cos(sideAngle1));
            int sideY1 = midY - (int)(radius * 0.3 * Math.sin(sideAngle1));
            int sideX2 = midX + (int)(radius * 0.3 * Math.cos(sideAngle2));
            int sideY2 = midY - (int)(radius * 0.3 * Math.sin(sideAngle2));
            g2d.drawLine(midX, midY, sideX1, sideY1);
            g2d.drawLine(midX, midY, sideX2, sideY2);
        }
        
        // 중앙 작은 원
        g2d.fillOval(centerX - 2, centerY - 2, 4, 4);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
}

