import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class MainWindowSplashScreen extends JFrame {

    MainWindow window;

    public MainWindowSplashScreen(String imagePath) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 349);
        setLocationRelativeTo(null);
        ImagePanel imagePanel = new ImagePanel(imagePath);
        add(imagePanel);
        setUndecorated(true);
        setVisible(true);
    }


    static class ImagePanel extends JPanel {
        private BufferedImage image;

        public ImagePanel(String imagePath) {
            try {
                image = ImageIO.read(new File(imagePath));
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();

                double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);

                int drawWidth = (int) (imgWidth * scale);
                int drawHeight = (int) (imgHeight * scale);

                int x = (panelWidth - drawWidth) / 2;
                int y = (panelHeight - drawHeight) / 2;

                g2.drawImage(image, x, y, drawWidth, drawHeight, this);

                g2.dispose();
            }
        }
    }

}
