import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SnowShare Client - Main Entry Point
 */
public class Main extends JFrame {
  private JPanel mainPanel;
  private JButton testButton;
  private JLabel statusLabel;

  public Main() {
    initializeComponents();
    setupLayout();
    setupEventHandlers();
  }

  private void initializeComponents() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    testButton = new JButton("Test Connection");
    statusLabel = new JLabel("Ready");
    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
  }

  private void setupLayout() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(testButton);

    mainPanel.add(statusLabel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  private void setupEventHandlers() {
    testButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            statusLabel.setText("Testing connection...");
            // TODO: Implement API connection test
            JOptionPane.showMessageDialog(
                Main.this,
                "API connection test - To be implemented",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Ready");
          }
        });
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            Main frame = new Main();
            frame.setTitle("SnowShare");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
          }
        });
  }
}

