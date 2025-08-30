import javax.swing.*;
import java.awt.*;

public class TestingLand extends JFrame {

    public TestingLand() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.RED);
                g2d.fillRect(100, 100, 100, 100);

            }
        };
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestingLand().setVisible(true));
    }
}

