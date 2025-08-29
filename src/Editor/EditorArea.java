package Editor;

// ---------------------- // Imports // ---------------------- //
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

// ---------------------- // Editor Area // ---------------------- //
public class EditorArea extends JPanel {

    // ---- public ---- //
    public double scale = 80;
    public double xPosition = 0;
    public double yPosition = 0;
    public double lastReleasedPositionX = 0;
    public double lastReleasedPositionY = 0;

    public int mouseXLocation = 0;
    public int mouseYLocation = 0;
    public int xDrag = 0;
    public int yDrag = 0;
    public int pressScreenY;
    public int pressScreenX;
    public int wireStartIndex = 0;

    public java.util.List<Wire> wires = new ArrayList<>();
    public ElectricalComponent wireStartComponent = null;
    public String creatingComponentID;

    public boolean inWireMode = false;
    public boolean creatingNewComponent;

    // ---- private ---- //
    protected History history;

    private static final int DRAG_THRESHOLD = 8;
    private boolean isDragging = false;

    private EditorBottomTaskbar taskbar;
    private EditorComponentInformationConfigurator informationConfigurator;
    private EditorHistoryTrackerList editorHistoryList;

    private Image creatingNewComponentImage;

    // ---------------------- // Constructor // ---------------------- //
    public EditorArea() {
        // ---- init vars ---- //
        creatingNewComponent = false;
        creatingNewComponentImage = null;

        history = new History(this);

        // ---- Swing Functions ---- //
        setLayout(null);
        setFocusable(true);

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

                // Handle clicks on the editor canvas here; A click is trigger when NOT dragging, not when clicked
                if (!isDragging) {
                    grabFocus(); // give focus to main editor on click

                    // create new component on mouse click
                    if (creatingNewComponent) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        try {
                            double x = ((pressScreenX - (scale / 2)) / scale) + xPosition;
                            double y = ((pressScreenY - (scale / 2)) / scale) + yPosition;
                            createNewComponent(x, y);
                        } catch (Exception ignored) {} // needed because of how fucked createNewComponent is
                    }

                    /* handles clicks on wires, checks for mouse clicks to the closest wire and
                       determines if it falls within the click threshold */
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
                // exit wiring mode if escape key is pressed
                if (inWireMode && e.getKeyCode() == 27) { //escape key
                    inWireMode = false;
                    wireStartComponent = null;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

                // remove selected wire if delete key is pressed (if one is selected)
                if (e.getKeyCode() == 127) { //delete key
                    Wire removedWire = wires.stream().filter(w -> w.isHighlighted).findAny().get();
                    removedWire.endComponent.disconnect(removedWire.startComponent);
                    wires.remove(removedWire);

                    //update edit history
                    history.addEvent(History.Event.DELETED_WIRE, removedWire.startIndex, removedWire.endIndex, removedWire);
                    repaint();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                /* Check if mouse movement passed the threshold to be considered a drag and not a click,
                    prevents slight movement right before a click registering as a drag */
                if (!isDragging) {
                    int dx = Math.abs(e.getX() - pressScreenX);
                    int dy = Math.abs(e.getY() - pressScreenY);
                    if (dx >= DRAG_THRESHOLD || dy >= DRAG_THRESHOLD) {
                        isDragging = true;
                    } else {
                        return;
                    }
                }

                // update drag+canvas position variables
                xDrag = (int) (xPosition + e.getX());
                yDrag = (int) (yPosition + e.getY());
                xPosition = lastReleasedPositionX - (xDrag - pressScreenX) / scale;
                yPosition = lastReleasedPositionY - (yDrag - pressScreenY) / scale;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //grab mouse screen coordinates
                mouseXLocation = e.getX();
                mouseYLocation = e.getY();

                // convert screen coordinates to "world" coords on canvas
                double x = (mouseXLocation / scale) + xPosition;
                double y = (mouseYLocation / scale) + yPosition;

                if (creatingNewComponent) repaint(); // only need redraw if mouse cursor is moving component
                taskbar.updateCursorPosReadout(x, y);
            }
        });


        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // reset the component value editor to display nothing on canvas clicked
                informationConfigurator.setComponent(null);
            }

            @Override
            public void focusLost(FocusEvent e) {
                //when clicking out of EditorArea, make all highlighted components lose focus/reset vars
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
            //handle mouse wheel scroll on canvas
            double oldScale = scale;
            double zoomFactor = e.getPreciseWheelRotation() < 0 ? 1.1 : 1 / 1.1;
            scale *= zoomFactor;
            if (scale < 1) scale = 1; // lock scroll to positive values


            //I forget what this does. Its probably important tho
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

        // do undo on ctrl-z
        KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlZ, "undoAction");
        getActionMap().put("undoAction", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
    }


    // ---------------------- // Helper Methods // ---------------------- //
    public Point worldToScreen(double worldX, double worldY) {
        int x = (int) ((worldX - xPosition) * scale);
        int y = (int) ((worldY - yPosition) * scale);
        return new Point(x, y);
    }

    public Point2D.Double screenToWorld(int screenX, int screenY) {
        double worldX = (screenX / scale) + xPosition;
        double worldY = (screenY / scale) + yPosition;
        return new Point2D.Double(worldX, worldY);
    }

    // ---------------------- // Component Editing Methods // ---------------------- //
    public void connectComponents(ElectricalComponent a, int aIndex, ElectricalComponent b, int bIndex) {
        Wire w = new Wire(a, aIndex, b, bIndex);
        wires.add(w);
        a.connect(b);
        history.addEvent(History.Event.CREATED_WIRE, w.startIndex, w.endIndex, w);
        repaint();
    }

    public void enableWireMode() {
        inWireMode = true;
        creatingNewComponent = false;
        creatingComponentID = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        grabFocus();
    }

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
            wire.endComponent.disconnect(wire.startComponent);
            history.addEvent(History.Event.DELETED_WIRE, wire.startIndex, wire.endIndex, wire);
        }
        if (!removedWires.isEmpty())history.addEvent(History.Event.DELETED_CONNECTING_WIRES, eC.x, eC.y, eC);
        repaint();
    }

    //TODO: fix loading images into memory at random fuck-ass places. All images should be static and loaded
    // once, then shared
    public void setCreatingNewComponent(String id) {
        grabFocus();
        creatingNewComponent = true;
        creatingComponentID = id;
        creatingNewComponentImage = new ImageIcon("resources/"+id+".png").getImage();
    }

    //TODO: This shits as fucked as it looks. Should find a better way to do this
    public void createNewComponent(double worldX, double worldY) throws
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class compClass = ElectricalComponentIdentifier.findClassFromID(creatingComponentID);
        history.addEvent(History.Event.CREATED_NEW_COMPONENT, worldX, worldY,
                compClass.getDeclaredConstructor(EditorArea.class, double.class, double.class)
                .newInstance(this, worldX, worldY));
    }


    public void undo() {
        HistoryEntry lastEvent = history.getLastAndRemove();
        if (lastEvent==null) return;

        switch (lastEvent.event) {

            //undo deleting a component that was attached to wires
            case History.Event.DELETED_CONNECTING_WIRES:
                    History.Event trackingEvent = null;
                    ArrayList<Wire> removedWires = new ArrayList<>();
                    do {
                        HistoryEntry historyEntry = history.getLastAndRemove();
                        trackingEvent = historyEntry.event;
                        if (trackingEvent == History.Event.DELETED_WIRE) {
                            removedWires.add((Wire) (historyEntry.component));
                            ((Wire) (historyEntry.component)).endComponent
                                    .connect(((Wire) (historyEntry.component)).startComponent);
                        }
                        if (trackingEvent == History.Event.DELETED_COMPONENT) {
                            ElectricalComponent component = (ElectricalComponent) historyEntry.component;
                            component.setDeleted(false);
                            add(component.getDraggableEditorComponent());
                            wires.addAll(removedWires);
                        }
                    } while (trackingEvent == History.Event.DELETED_WIRE);
            break;

            case History.Event.DELETED_COMPONENT:
                ElectricalComponent component = (ElectricalComponent) lastEvent.component;
                component.setDeleted(false);
                add(component.getDraggableEditorComponent());
            break;

            case History.Event.DELETED_WIRE:
                Wire wire = (Wire) lastEvent.component;
                wires.add(wire);
                ((Wire) lastEvent.component).endComponent.connect(((Wire) lastEvent.component).startComponent);
            break;

            case History.Event.CREATED_NEW_COMPONENT:
                ElectricalComponent component1 = (ElectricalComponent) lastEvent.component;
                remove(component1.getDraggableEditorComponent());
                component1.setDeleted(true);
            break;

            case History.Event.CREATED_WIRE:
                Wire wire1 = (Wire) lastEvent.component;
                ((Wire) lastEvent.component).endComponent.disconnect(((Wire) lastEvent.component).startComponent);
                wires.remove(wire1);
            break;

            case History.Event.MOVED_COMPONENT:
                ElectricalComponent component2 = (ElectricalComponent) lastEvent.component;
                component2.getDraggableEditorComponent()
                        .setWorldPosition(lastEvent.editLocationX, lastEvent.editLocationY);

            case History.Event.ROTATED_COMPONENT:
                DraggableEditorComponent component3 = ((ElectricalComponent) lastEvent.component)
                        .getDraggableEditorComponent();
                component3.orientation = lastEvent.rotation;
                ((ElectricalComponent) lastEvent.component).rotateConnectionPoints(lastEvent.rotation);
            break;
        }
        repaint();
    }

    public void redo() {
        HistoryEntry lastEvent = history.getFutureAndRemove();
        if (lastEvent==null) return;
        System.out.println(lastEvent.event);
        //redo deleting a component that was attached to wires
        //TODO: this shit don't fucking work
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



    // ---------------------- // Draw all graphics // ---------------------- //
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Paint background grid
        //TODO: fix 1800 workaround. should be dynamic, need to do some kind of call to
        // figure out current max viewport size
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

        //Paint all DraggableEditorComponent objects
        for (Component c : getComponents()) {
            if (c instanceof DraggableEditorComponent) {
                ((DraggableEditorComponent) c).updateFromEditor();
            }
        }

        // if currently creating a new component, draw that component on the mouse and set cursor to plus
        if (creatingNewComponent) {
            try {
                g.drawImage(creatingNewComponentImage, (int) (getMousePosition().x - (scale / 2)),
                        (int) (getMousePosition().y - scale / 2), (int) scale, (int) scale, this);
            } catch (NullPointerException ignored) {} //the cursor goes off the screen
            this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        //Paint all wires
        g.setColor(Color.BLACK);
        for (Wire wire : wires) {
            wire.draw((Graphics2D) g, this);
        }
    }


    // ---------------------- // Getter+Setter Methods // ---------------------- //
    public boolean getInWireMode() {
        return inWireMode;
    }

    public EditorComponentInformationConfigurator getInformationConfigurator() {
        return informationConfigurator;
    }

    public void setTaskBar(EditorBottomTaskbar taskbar) {
        this.taskbar = taskbar;
    }

    public void setInformationConfigurator(EditorComponentInformationConfigurator iC) {
        informationConfigurator = iC;
    }

    public void setEditorHistoryList(EditorHistoryTrackerList editorHistoryList) {
        this.editorHistoryList =editorHistoryList;
        history.setEditorHistoryList(editorHistoryList);
    }
}