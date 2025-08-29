package Editor;

import Editor.History.History;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

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
                        mainEditor.undo();
                    } else if (text.equals("redo")) {
                        mainEditor.redo();
                    } else if (text.equals("zoomin") || text.equals("zoomout")) {
                        double oldScale = mainEditor.scale;
                        mainEditor.scale*= text.equals("zoomout")?1/1.5:1.5;
                        Point center = new Point(mainEditor.getWidth()/2, mainEditor.getHeight()/2);
                        double mouseWorldXBefore = (center.x / oldScale) + mainEditor.xPosition;
                        double mouseWorldYBefore = (center.y / oldScale) + mainEditor.yPosition;

                        double mouseWorldXAfter = (center.x / mainEditor.scale) + mainEditor.xPosition;
                        double mouseWorldYAfter = (center.y / mainEditor.scale) + mainEditor.yPosition;

                        mainEditor.xPosition += (mouseWorldXBefore - mouseWorldXAfter);
                        mainEditor.yPosition += (mouseWorldYBefore - mouseWorldYAfter);

                        mainEditor.lastReleasedPositionX = mainEditor.xPosition;
                        mainEditor.lastReleasedPositionY = mainEditor.yPosition;

                        mainEditor.repaint();
                    } else if (text.equals("delete")) {
                        //if it's a component needed to be deleted
                        DraggableEditorComponent removeComponent = mainEditor.getFocusedComponent();
                        if (removeComponent!=null) {
                            mainEditor.deleteComponent(removeComponent.getElectricalComponent());
                        }
                        //duplicated from EditorArea under key 127 (wires)
                        Optional<Wire> removedWire = mainEditor.wires.stream().filter(w -> w.isHighlighted).findAny();
                        if (removedWire.isPresent()) {
                            Wire rmWire = removedWire.get();
                            mainEditor.wires.remove(rmWire);
                            rmWire.endComponent.disconnect(rmWire.startComponent);
                            mainEditor.history.addEvent(History.Event.DELETED_WIRE, rmWire.startIndex, rmWire.endIndex, rmWire);
                            mainEditor.repaint();
                        }
                    } else if (text.equals("zoomcenter")) {
                        //resets zoom to starting values
                        mainEditor.xPosition = 0;
                        mainEditor.yPosition = 0;
                        mainEditor.scale = 80;
                        mainEditor.lastReleasedPositionX = mainEditor.xPosition;
                        mainEditor.lastReleasedPositionY = mainEditor.yPosition;
                        mainEditor.repaint();
                    } else if (text.equals("zoomfit")) {
                        //fits all components onto the screen and centers
                        double top = Double.MAX_VALUE;
                        double bottom = Double.MIN_VALUE;
                        double left = Double.MAX_VALUE;
                        double right = Double.MIN_VALUE;
                        if (mainEditor.getComponents().length == 0) return;
                        for (Component c : mainEditor.getComponents()) {
                            if (c instanceof DraggableEditorComponent) {
                                System.out.println(c);
                                DraggableEditorComponent dec = ((DraggableEditorComponent)c);
                                double cx = dec.getWorldX();
                                double cy = dec.getWorldY();
                                left = Math.min(left, cx);
                                right = Math.max(right, cx+dec.getWidth()/ mainEditor.scale);
                                top = Math.min(top, cy);
                                bottom = Math.max(bottom, cy+(dec.getHeight()/ mainEditor.scale));
                            }
                        }
                        if (left==right && bottom==top) {
                            mainEditor.xPosition = left - (mainEditor.getWidth() / (2.0 * mainEditor.scale));
                            mainEditor.yPosition = top - (mainEditor.getHeight() / (2.0 * mainEditor.scale));

                            mainEditor.lastReleasedPositionY = mainEditor.yPosition;
                            mainEditor.lastReleasedPositionX = mainEditor.xPosition;
                        }
                        double worldWidth = (right - left) * 1.05;
                        double worldHeight = (bottom - top)* 1.05;

                        double scaleX = (mainEditor.getWidth()) / worldWidth;
                        double scaleY = (mainEditor.getHeight()) / worldHeight;
                        mainEditor.scale = Math.min(scaleX, scaleY);

                        double worldCenterX = (left + right) / 2.0;
                        double worldCenterY = (top + bottom) / 2.0;

                        mainEditor.xPosition = worldCenterX - (mainEditor.getWidth() / (2.0 * mainEditor.scale));
                        mainEditor.yPosition = worldCenterY - (mainEditor.getHeight() / (2.0 * mainEditor.scale));

                        mainEditor.lastReleasedPositionY = mainEditor.yPosition;
                        mainEditor.lastReleasedPositionX = mainEditor.xPosition;
                        mainEditor.repaint();
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
