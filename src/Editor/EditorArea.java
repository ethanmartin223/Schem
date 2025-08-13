package Editor;

import Editor.History.History;
import Editor.History.HistoryEntry;
import ElectronicsBackend.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EditorArea extends JPanel {
    public double scale = 80;
    public double xPosition = 0;
    public double yPosition = 0;

    private static final int DRAG_THRESHOLD = 8;
    private boolean isDragging = false;
    public int pressScreenX;
    public int pressScreenY;

    protected History history;

    //new shit
    public java.util.List<Wire> wires = new ArrayList<>();
    public ElectricalComponent wireStartComponent = null;
    public int wireStartIndex = 0;

    public boolean inWireMode = false;

    public double lastReleasedPositionX = 0;
    public double lastReleasedPositionY = 0;

    public boolean creatingNewComponent;

    public String creatingComponentID;

    public int xPress = 0;
    public int yPress = 0;
    public int xDrag = 0;
    public int yDrag = 0;
    private EditorBottomTaskbar taskbar;
    private Image creatingNewComponentImage;

    private EditorComponentInformationConfigurator informationConfigurator;
    private EditorHistoryTrackerList editorHistoryList;

    public EditorComponentInformationConfigurator getInformationConfigurator() {
        return informationConfigurator;
    }

    public void setInformationConfigurator(EditorComponentInformationConfigurator iC) {
        informationConfigurator = iC;
    }

    public void connectComponents(ElectricalComponent a, int aIndex, ElectricalComponent b, int bIndex) {
        Wire w = new Wire(a, aIndex, b, bIndex);
        wires.add(w);
        history.addEvent(History.Event.CREATED_WIRE, w.startIndex, w.endIndex, w);
        repaint();
    }

    public Point worldToScreen(double worldX, double worldY) {
        int x = (int) ((worldX - xPosition) * scale);
        int y = (int) ((worldY - yPosition) * scale);
        return new Point(x, y);
    }

    public void enableWireMode() {
        inWireMode = true;
        creatingNewComponent = false;
        creatingComponentID = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        grabFocus();
    }

    public void undo() {
        HistoryEntry lastEvent = history.getLastAndRemove();
        if (lastEvent==null) return;

        //undo deleting a component that was attached to wires
        if (lastEvent.event == History.Event.DELETED_CONNECTING_WIRES) {
            History.Event trackingEvent = null;
            ArrayList<Wire> removedWires = new ArrayList<>();
            do {
                HistoryEntry historyEntry = history.getLastAndRemove();
                trackingEvent = historyEntry.event;
                if (trackingEvent == History.Event.DELETED_WIRE) {
                    removedWires.add((Wire)(historyEntry.component));
                }
                if (trackingEvent == History.Event.DELETED_COMPONENT) {
                    ElectricalComponent component = (ElectricalComponent) historyEntry.component;
                    component.setDeleted(false);
                    add(component.getDraggableEditorComponent());
                    wires.addAll(removedWires);
                }
            } while (trackingEvent == History.Event.DELETED_WIRE);
        }

        // undo deleting component
        if (lastEvent.event == History.Event.DELETED_COMPONENT) {
            ElectricalComponent component = (ElectricalComponent) lastEvent.component;
            component.setDeleted(false);
            add(component.getDraggableEditorComponent());
        }

        // undo deleting wire
        if (lastEvent.event == History.Event.DELETED_WIRE) {
            Wire wire = (Wire) lastEvent.component;
            wires.add(wire);
        }

        // undo creating component
        if (lastEvent.event == History.Event.CREATED_NEW_COMPONENT) {
            ElectricalComponent component = (ElectricalComponent) lastEvent.component;
            remove(component.getDraggableEditorComponent());
            component.setDeleted(true);
        }

        // undo creating wire
        if (lastEvent.event == History.Event.CREATED_WIRE) {
            Wire wire = (Wire) lastEvent.component;
            wires.remove(wire);
        }

        repaint();
    }

    public void redo() {
        HistoryEntry lastEvent = history.getFutureAndRemove();
        if (lastEvent==null) return;
        System.out.println(lastEvent.event);
        //redo deleting a component that was attached to wires
        if (history.getLastFromFuture().event== History.Event.DELETED_CONNECTING_WIRES) {
            ElectricalComponent component = (ElectricalComponent) lastEvent.component;
            component.setDeleted(true);
            remove(component.getDraggableEditorComponent());

            List<Wire> removedWires = wires.stream()
                    .filter(w -> w.endComponent == component || w.startComponent == component)
                    .toList();
            wires.removeAll(removedWires);

        }

        // redo deleting component
        if (lastEvent.event == History.Event.DELETED_COMPONENT) {
            ElectricalComponent component = (ElectricalComponent) lastEvent.component;
            component.setDeleted(true);
            remove(component.getDraggableEditorComponent());
        }

        // redo deleting wire
        if (lastEvent.event == History.Event.DELETED_WIRE) {
            Wire wire = (Wire) lastEvent.component;
            wires.remove(wire);
        }

        // redo creating component
        if (lastEvent.event == History.Event.CREATED_NEW_COMPONENT) {
            ElectricalComponent component = (ElectricalComponent) lastEvent.component;
            component.setDeleted(false);
            add(component.getDraggableEditorComponent());
        }

        // redo creating wire
        if (lastEvent.event == History.Event.CREATED_WIRE) {
            Wire wire = (Wire) lastEvent.component;
            wires.add(wire);
        }

        editorHistoryList.addEntry(lastEvent.event+" " +lastEvent.componentType.getCanonicalName());
        repaint();
    }

    public EditorArea() {
        setLayout(null);
        setFocusable(true);

        creatingNewComponent = false;
        creatingNewComponentImage = null;

        history = new History(this);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressScreenX = e.getX();
                pressScreenY = e.getY();
                isDragging = false; // reset drag state
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastReleasedPositionX = xPosition;
                lastReleasedPositionY = yPosition;

                // Only treat as click if not dragging
                if (!isDragging) {
                    grabFocus();
                    if (creatingNewComponent) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        try {
                            double x = ((pressScreenX - (scale / 2)) / scale) + xPosition;
                            double y = ((pressScreenY - (scale / 2)) / scale) + yPosition;
                            createNewComponent(x, y);
                        } catch (Exception ignored) {}
                    }
                    boolean foundWire = false;
                    for (Wire wire : wires) {
                        if (wire.isNear(screenToWorld(e.getX(), e.getY())) && !foundWire) {
                            wire.setFocus();
                            foundWire = true;
                            continue;
                        }
                        wire.loseFocus();
                    }
                    creatingNewComponent = false;
                    creatingComponentID = null;
                    repaint();
                }
            }
        });


        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (inWireMode && e.getKeyCode() == 27) { //escape key
                    inWireMode = false;
                    wireStartComponent = null;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } if (e.getKeyCode() == 127) { //delete key
                    Wire removedWire = wires.stream().filter(w -> w.isHighlighted).findAny().get();
                    wires.remove(removedWire);
                    history.addEvent(History.Event.DELETED_WIRE, removedWire.startIndex, removedWire.endIndex, removedWire);
                    repaint();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Check if movement passed the threshold
                if (!isDragging) {
                    int dx = Math.abs(e.getX() - pressScreenX);
                    int dy = Math.abs(e.getY() - pressScreenY);
                    if (dx >= DRAG_THRESHOLD || dy >= DRAG_THRESHOLD) {
                        isDragging = true;
                    } else {
                        return;
                    }
                }

                xDrag = (int) (xPosition + e.getX());
                yDrag = (int) (yPosition + e.getY());
                xPosition = lastReleasedPositionX - (xDrag - pressScreenX) / scale;
                yPosition = lastReleasedPositionY - (yDrag - pressScreenY) / scale;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xPress = e.getX();
                yPress = e.getY();

                double x = (xPress / scale) + xPosition;
                double y = (yPress / scale) + yPosition;

                if (creatingNewComponent) repaint();
                taskbar.updateCursorPosReadout(x, y);
            }
        });


        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                informationConfigurator.setComponent(null);
            }

            @Override
            public void focusLost(FocusEvent e) {
                creatingNewComponent = false;
                creatingComponentID = null;
                inWireMode = false;
                wireStartComponent = null;
                for (Wire wire : wires) {
                    wire.loseFocus();
                }
                repaint();
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        this.addMouseWheelListener(e -> {
            double oldScale = scale;
            double zoomFactor = e.getPreciseWheelRotation() < 0 ? 1.1 : 1 / 1.1;
            scale *= zoomFactor;
            if (scale < 1) scale = 1;

            Point mouse = e.getPoint();
            double mouseWorldXBefore = (mouse.x / oldScale) + xPosition;
            double mouseWorldYBefore = (mouse.y / oldScale) + yPosition;

            double mouseWorldXAfter = (mouse.x / scale) + xPosition;
            double mouseWorldYAfter = (mouse.y / scale) + yPosition;

            xPosition += (mouseWorldXBefore - mouseWorldXAfter);
            yPosition += (mouseWorldYBefore - mouseWorldYAfter);

            lastReleasedPositionX = xPosition;
            lastReleasedPositionY = yPosition;
            repaint();
        });
    }

    public Point2D.Double screenToWorld(int screenX, int screenY) {
        double worldX = (screenX / scale) + xPosition;
        double worldY = (screenY / scale) + yPosition;
        return new Point2D.Double(worldX, worldY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.LIGHT_GRAY);
        int gridSize = (int) scale;

        for (int x = (int) -scale; x <= getWidth() + (int) scale; x += gridSize) {
            int screenX = (int) (x - scale * xPosition % gridSize);
            g.drawLine(screenX, 0, screenX, 1800);
        }
        for (int y = (int) -scale; y <= getHeight() + (int) scale; y += gridSize) {
            int screenY = (int) (y - scale * yPosition % gridSize);
            g.drawLine(0, screenY, 1800, screenY);
        }

        for (Component c : getComponents()) {
            if (c instanceof DraggableEditorComponent) {
                ((DraggableEditorComponent) c).updateFromEditor();
            }
        }

        if (creatingNewComponent) {
           try {
                g.drawImage(creatingNewComponentImage, (int) (getMousePosition().x - (scale / 2)),
                        (int) (getMousePosition().y - scale / 2), (int) scale, (int) scale, this);
            } catch (NullPointerException ignored) {
            } //the cursor goes off the screen
            this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        g.setColor(Color.BLACK);
        for (Wire wire : wires) {
            wire.draw((Graphics2D) g, this);
        }
    }

    public void setTaskBar(EditorBottomTaskbar taskbar) {
        this.taskbar = taskbar;
    }

    public void setCreatingNewComponent(String id) {
        grabFocus();
        creatingNewComponent = true;
        creatingComponentID = id;
        creatingNewComponentImage = new ImageIcon("resources/"+id+".png").getImage();
    }

    public Object createNewComponent(double worldX, double worldY) throws
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class compClass = ElectricalComponentIdentifier.findClassFromID(creatingComponentID);
        Object t;
        history.addEvent(History.Event.CREATED_NEW_COMPONENT, worldX, worldY, t=compClass.getDeclaredConstructor(EditorArea.class, double.class, double.class)
                .newInstance(this, worldX, worldY));
        return t;
    }

    private ElectricalComponent owner;
    public void setOwner(ElectricalComponent ec) { this.owner = ec; }
    public ElectricalComponent getOwner() { return owner; }
    public boolean isInWireMode() { return inWireMode; }

    public void deleteComponent(ElectricalComponent eC) {
        grabFocus();
        history.addEvent(History.Event.DELETED_COMPONENT, eC.x, eC.y, eC);
        remove(eC.getDraggableEditorComponent());
        eC.setDeleted(true);
        List<Wire> removedWires = wires.stream()
                .filter(w -> w.endComponent == eC || w.startComponent == eC)
                .toList();
        for (Wire wire : removedWires) {
            wires.remove(wire);
            history.addEvent(History.Event.DELETED_WIRE, wire.startIndex, wire.endIndex, wire);
        }
        if (!removedWires.isEmpty())history.addEvent(History.Event.DELETED_CONNECTING_WIRES, eC.x, eC.y, eC);
        repaint();
    }

    public void createNewComponent(String id, int x, int y) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        creatingComponentID = id;
        createNewComponent(x, y);
    }

    public void setEditorHistoryList(EditorHistoryTrackerList editorHistoryList) {
        this.editorHistoryList =editorHistoryList;
        history.setEditorHistoryList(editorHistoryList);
    }
}