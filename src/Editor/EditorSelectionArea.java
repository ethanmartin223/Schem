package Editor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;

class EditorSelectionArea {
    public boolean isDragging;
    private int startDragX, startDragY;
    private int currentX, currentY;
    private final Color selectColor = new Color(255, 153, 0, 128);
    private final Color selectBorderColor = new Color(255, 153, 0, 255);
    private final TexturePaint stipple;
    private EditorArea editor;

    public HashSet<DraggableEditorComponent> multiSelected;

    public EditorSelectionArea(EditorArea editorArea) {
        isDragging = false;
        editor = editorArea;
        startDragX = Integer.MAX_VALUE;
        startDragY = Integer.MAX_VALUE;
        currentX = Integer.MAX_VALUE;
        currentY = Integer.MAX_VALUE;

        multiSelected = new HashSet<>();

        BufferedImage stippleImage = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < stippleImage.getHeight(); y++) {
            for (int x = 0; x < stippleImage.getWidth(); x++) {
                if ((x ^ y) == 0) {
                    stippleImage.setRGB(x, y, selectColor.getRGB());
                }
            }
        }
        stipple = new TexturePaint(stippleImage, new Rectangle(0, 0, stippleImage.getWidth(), stippleImage.getHeight()));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3) {
                    editor.grabFocus();
                    isDragging = true;
                    startDragX = e.getX();
                    startDragY = e.getY();
                    currentX = startDragX;
                    currentY = startDragY;
                    editor.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    currentX = e.getX();
                    currentY = e.getY();
                    editor.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton()==3 && isDragging) {
                    clearMultiSelected();
                    for (Wire w : (editor.wires)) {
                        w.loseFocus();
                    }

                    isDragging = false;
                    Rectangle bounds = new Rectangle(startDragX, startDragY,
                            currentX-startDragX, currentY-startDragY);
                    for (Component c : editor.getComponents()) {
                        if (c instanceof DraggableEditorComponent component) {
                            if (bounds.getBounds().contains(component.getX()+component.getWidth()/2,
                                    component.getY()+component.getHeight()/2)) {
                                component.isMultiSelected = true;
                                multiSelected.add(component);
                            }
                        }
                    }
                    for (Wire w : (editor.wires)) {
                        DraggableEditorComponent a = w.getStartComponent().getDraggableEditorComponent();
                        DraggableEditorComponent b = w.getEndComponent().getDraggableEditorComponent();
                        if (bounds.getBounds().contains(a.getX()+a.getWidth()/2,a.getY()+a.getHeight()/2) &&
                            (bounds.getBounds().contains(b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2))){
                             w.isMultiSelected = true;
                        }
                    }

                    editor.repaint();
                }
            }
        };

        editor.addMouseListener(mouseHandler);
        editor.addMouseMotionListener(mouseHandler);
    }

    public void clearMultiSelected() {
        for (DraggableEditorComponent component : multiSelected) {
            component.isMultiSelected = false;
        }
        for (Wire w : editor.wires) {
            w.isMultiSelected = false;
        }
        multiSelected.clear();
        editor.paintImmediately(0,0,editor.getWidth(), editor.getHeight());
    }

    public void paint(Graphics2D g2d) {
        if (isDragging) {
            int x = Math.min(startDragX, currentX);
            int y = Math.min(startDragY, currentY);
            int width = Math.abs(currentX - startDragX);
            int height = Math.abs(currentY - startDragY);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2d.setPaint(stipple);
            g2d.fillRoundRect(x, y, width, height, 5, 5);
            g2d.setColor(selectBorderColor);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(x, y, width, height, 5, 5);
        }
    }
}
