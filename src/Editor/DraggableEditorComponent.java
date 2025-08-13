package Editor;

import ElectronicsBackend.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class DraggableEditorComponent extends JComponent {
    private final EditorArea editor;
    private final Image image;
    private final Image selectedImage;
    public double boundsOverride;

    private Image currentDisplayedImage;

    private double worldX, worldY;
    private double offsetX, offsetY;
    private boolean dragging = false;

    public int orientation;
    private ElectricalComponent electricalComponent;

    public DraggableEditorComponent(EditorArea editor, Image image, Image selectedImage, double worldX, double worldY, ElectricalComponent parentElectricalComponent) {
        this.editor = editor;

        this.electricalComponent = parentElectricalComponent;

        this.image = image;
        this.currentDisplayedImage = image;
        this.selectedImage = selectedImage;

        this.worldX = worldX;
        this.worldY = worldY;

        this.boundsOverride = 1;

        this.orientation = 0;

        updateBounds();
        setOpaque(false);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyChar() == 'r') {
                    orientation += 1;
                    if (orientation > 3) orientation = 0;
                    parentElectricalComponent.rotateConnectionPoints(orientation);
                    editor.repaint(); // to avoid wire not getting repainted
                }
                if (e.getKeyCode() == 127) { //delete key
                    editor.deleteComponent(parentElectricalComponent);
                }
            }
        });

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                EditorComponentInformationConfigurator ecic = editor.getInformationConfigurator();
                ecic.setComponent(parentElectricalComponent);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });


        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!editor.creatingNewComponent) {
                    if (editor.getInWireMode()) {
                        ElectricalComponent thisComponent = electricalComponent;

                        int nearestPinIndex = getNearestConnectionPointIndex(e.getPoint());

                        if (editor.wireStartComponent == null) {
                            editor.wireStartComponent = thisComponent;
                            editor.wireStartIndex = nearestPinIndex;
                        } else {
                            editor.connectComponents(editor.wireStartComponent, editor.wireStartIndex, thisComponent, nearestPinIndex);
                            editor.wireStartComponent = null;
                            editor.inWireMode = false;
                            editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            editor.grabFocus();
                        }
                    }
                    else if (e.getButton() == 1 && !editor.inWireMode) {
                        grabFocus();
                    } else if (e.getButton() == 3) {
                        new EditorComponentContextMenu().show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!editor.creatingNewComponent) {
                    Point panelPoint = SwingUtilities.convertPoint(DraggableEditorComponent.this, e.getPoint(), editor);
                    Point2D.Double worldPoint = screenToWorld(panelPoint.x, panelPoint.y);

                    double imgWidthWorld = currentDisplayedImage.getWidth(null) / (editor.scale);
                    double imgHeightWorld = currentDisplayedImage.getHeight(null) / (editor.scale);

                    Rectangle2D.Double worldBounds = new Rectangle2D.Double(
                            getWorldX(), getWorldY(), imgWidthWorld, imgHeightWorld
                    );

                    if (worldBounds.contains(worldPoint)) {
                        dragging = true;
                        offsetX = worldPoint.x - getWorldX();
                        offsetY = worldPoint.y - getWorldY();
                    } else {
                        dispatchToParent(e);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                dispatchToParent(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    Point panelPoint = SwingUtilities.convertPoint(DraggableEditorComponent.this, e.getPoint(), editor);
                    Point2D.Double worldPoint = screenToWorld(panelPoint.x, panelPoint.y);
                    setWorldPosition(worldPoint.x - offsetX, worldPoint.y - offsetY);
                } else {
                    dispatchToParent(e);
                }
                editor.repaint();
            }


            @Override
            public void mouseMoved(MouseEvent e) {
                dispatchToParent(e);
            }
        });
    }

    private DraggableEditorComponent getSelf() {
        return this;
    }

    private void dispatchToParent(MouseEvent e) {
        Component parent = getParent();
        if (parent != null) {
            MouseEvent parentEvent = SwingUtilities.convertMouseEvent(this, e, parent);
            parent.dispatchEvent(parentEvent);
        }
    }

    private int getNearestConnectionPointIndex(Point clickedPoint) {
        ArrayList<Point2D.Double> worldPoints = electricalComponent.getConnectionPointsAsWorldPoints();

        Point2D.Double clickWorld = screenToWorld(
                SwingUtilities.convertPoint(this, clickedPoint, editor).x,
                SwingUtilities.convertPoint(this, clickedPoint, editor).y
        );

        int nearestIndex = 0;
        double minDistSq = Double.MAX_VALUE;

        for (int i = 0; i < worldPoints.size(); i++) {
            Point2D.Double wp = worldPoints.get(i);
            double dx = wp.x - clickWorld.x;
            double dy = wp.y - clickWorld.y;
            double distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }


    public void setWorldPosition(double x, double y) {
        this.worldX = x;
        this.worldY = y;
        updateBounds();
        editor.repaint();
    }


    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    private Point2D.Double screenToWorld(int screenX, int screenY) {
        double x = (screenX / editor.scale) + editor.xPosition;
        double y = (screenY / editor.scale) + editor.yPosition;
        return new Point2D.Double(x, y);
    }

    private Point worldToScreen(double worldX, double worldY) {
        int x = (int) ((worldX - editor.xPosition) * editor.scale);
        int y = (int) ((worldY - editor.yPosition) * editor.scale);
        return new Point(x, y);
    }

    private void updateBounds() {
        Point screen = worldToScreen(worldX, worldY);
        int width = (int) (editor.scale * boundsOverride);
        int height = (int) (editor.scale * boundsOverride);
        int offsetX = (int) ((width - editor.scale) / 2.0);
        int offsetY = (int) ((height - editor.scale) / 2.0);
        setBounds(screen.x - offsetX, screen.y - offsetY, width, height);
        electricalComponent.x = worldX;
        electricalComponent.y = worldY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Image imgToDraw = isFocusOwner() ? selectedImage : image;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int size = (int) editor.scale;

        int drawX = (getWidth() - size) / 2;
        int drawY = (getHeight() - size) / 2;

        if (orientation != 0) {
            g2d.rotate(Math.toRadians(orientation * -90), getWidth() / 2.0, getHeight() / 2.0);
        }

        g2d.drawImage(imgToDraw, drawX, drawY, size, size, this);
        g2d.dispose();
    }


    public void updateFromEditor() {
        updateBounds();
        //repaint(); problem here
    }

    public ElectricalComponent getElectricalComponent() {
        return electricalComponent;
    }
}
