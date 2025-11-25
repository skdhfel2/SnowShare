package javaProject;

import javax.swing.*;
import java.awt.*;

/**
 * SnowShare Client - 관련뉴스 모듈
 */
public class Main extends JFrame {
  
  public Main() {
    setupLookAndFeel();
    setupLayout();
  }
  
  /**
   * Look and Feel 설정 및 UI 테마 적용
   */
  private void setupLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      
      Color primaryColor = new Color(70, 130, 180);
      Color secondaryColor = new Color(135, 206, 250);
      Color backgroundColor = new Color(245, 248, 250);
      Color textColor = new Color(50, 50, 50);
      
      UIManager.put("Button.background", primaryColor);
      UIManager.put("Button.foreground", Color.WHITE);
      UIManager.put("Button.select", secondaryColor);
      UIManager.put("Panel.background", backgroundColor);
      UIManager.put("TextField.background", Color.WHITE);
      UIManager.put("TextField.foreground", textColor);
      UIManager.put("List.background", Color.WHITE);
      UIManager.put("List.selectionBackground", secondaryColor);
      UIManager.put("List.selectionForeground", Color.WHITE);
      UIManager.put("TextArea.background", Color.WHITE);
      UIManager.put("TextArea.foreground", textColor);
      UIManager.put("TabbedPane.background", backgroundColor);
      UIManager.put("TabbedPane.selected", primaryColor);
      
    } catch (Exception e) {
      System.err.println("Look and Feel 설정 실패: " + e.getMessage());
    }
  }

  private void setupLayout() {
    NewsPanel newsPanel = new NewsPanel();
    add(newsPanel);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            Main frame = new Main();
            frame.setTitle("관련뉴스");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setBackground(new Color(245, 248, 250));
            frame.setVisible(true);
          }
        });
  }
}

