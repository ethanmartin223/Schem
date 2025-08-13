package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditorTopToolBar extends JPanel {

    public EditorArea mainEditor;

    public EditorTopToolBar(EditorArea mainEditor) {
        this.mainEditor = mainEditor;

        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        addToolbarLabel("open", "Open file", "resources/menuAssets/open.png");
        addToolbarLabel("newfile", "New file", "resources/menuAssets/newFile.png");
        addToolbarLabel("save", "Save file", "resources/menuAssets/save.png");

        addToolbarLabel("undo", "Redo action", "resources/menuAssets/undo.png");
        addToolbarLabel("redo", "Redo action", "resources/menuAssets/redo.png");
        addToolbarLabel("delete", "Delete Component", "resources/menuAssets/delete.png");

        addToolbarLabel("run", "Run Simulation", "resources/menuAssets/run.png");
        addToolbarLabel("hist", "Open Edit History", "resources/menuAssets/history.png");

        addToolbarLabel("zoomin", "Zoom In", "resources/menuAssets/zoom.png");
        addToolbarLabel("zoomout", "Zoom out", "resources/menuAssets/shrink.png");
        addToolbarLabel("zoomcenter", "Zoom Center", "resources/menuAssets/zoomReset.png");
        addToolbarLabel("zoomfit", "Zoom to Fit all components", "resources/menuAssets/zoomfit.png");

    }

    private void addToolbarLabel(String text, String tooltip, String iconPath) {
        ImageIcon icon = loadIcon(iconPath);
        ClickableLabel label = new ClickableLabel(text, icon, mainEditor);
        label.setToolTipText(tooltip);
        add(label);
    }

    private ImageIcon loadIcon(String path) {
        try {
            return new ImageIcon(path);
        } catch (Exception e) {
            return null;
        }
    }

    static class ClickableLabel extends JLabel {
        private boolean hovered = false;
        private ImageIcon icon;

        public ClickableLabel(String text, ImageIcon icon, EditorArea mainEditor) {
            this.icon = icon;
            setPreferredSize(new Dimension(40, 40));
            setOpaque(false);
            setFocusable(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("undo")) {
                        System.out.println("UNDO FIRED");
                        mainEditor.undo();
                    } else if (text.equals("redo")) {
                        System.out.println("REDO FIRED");
                        mainEditor.redo();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (hovered) {
                g2.setColor(new Color(255, 190, 0));
            } else {
                g2.setColor(new Color(245, 245, 245));
            }
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (icon != null) {
                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, x, y);
            }

            g2.dispose();
        }
    }
}
