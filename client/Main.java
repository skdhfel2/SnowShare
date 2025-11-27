import javax.swing.*;

/**
 * SnowShare Client - 메인 진입점
 * App 클래스를 통해 전체 애플리케이션을 시작합니다.
 */
public class Main {
    public static void main(String[] args) {
        // Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }
        
        // Swing 이벤트 디스패치 스레드에서 애플리케이션 시작
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}

