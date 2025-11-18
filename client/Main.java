import javax.swing.*;

/**
 * 프로그램 실행 시작점
 * - JFrame 생성
 * - 초기 화면 호출
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new App();
        });
    }
}

