
package Editor;

// ---------------------- // Imports // ---------------------- //
import Editor.History.History;
import Editor.History.HistoryEntry;
import ElectricalComponents.Ground;
import ElectricalComponents.PowerSupply;
import ElectronicsBackend.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.VolatileImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static Editor.ComponentRenderer.clearBuffer;

// ---------------------- // Editor Area // ---------------------- //
public class EditorArea extends JPanel {
    private VolatileImage buffer;

    private static final Font FPS_FONT = new Font("Arial", Font.BOLD, 14);

    //static shared vars
    public EditorBottomTaskbar taskbar;

    // ---- FPS Tracking ---- //
    private long lastTime = System.nanoTime();
    private double fps = 0;
    private final double smoothing = .6; // higher = smoother/slower updates

    public static float DEBUG_NATIVE_DRAW_SIZE = .01f;

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

    public EditorSelectionArea selectedArea;
    public EditorSaveManager saveManager;
    public JFrame mainWindow;
    public java.util.List<Wire> wires = new ArrayList<>();
    public ElectricalComponent wireStartComponent = null;
    public String creatingComponentID;

    public boolean inWireMode = false;
    public boolean creatingNewComponent;
    public File currentlyEditingFile;

    // ---- private ---- //
    protected History history;

    private static final int DRAG_THRESHOLD = 8;
    private boolean isDragging = false;

    private EditorComponentInformationConfigurator informationConfigurator;
    private EditorHistoryTrackerList editorHistoryList;

    private Image creatingNewComponentImage;
    DraggableEditorComponent currentFocusedComponent;

    private EditorQuickEntryField quickEntry;

    //grid sub-divider lines (less than 1.0 world unit lines)
    private double tolerance = 1e-6; // floating-point tolerance
    private float minScaleForGridLinesAppearing = 80f;    // scale at which lines start appearing
    private float maxScaleForGridLinesAppearing = 200f;   // scale at which lines are fully visible
    boolean debugDrawMode = false;

    // ---------------------- // Constructor // ---------------------- //
    public EditorArea(JFrame parent) {
        // ---- init vars ---- //
        creatingNewComponent = false;
        creatingNewComponentImage = null;
        currentFocusedComponent = null;
        currentlyEditingFile = null;
        mainWindow = parent;

        selectedArea = new EditorSelectionArea(this);
        history = new History(this);
        quickEntry = new EditorQuickEntryField(this);
        add(quickEntry);
        saveManager = new EditorSaveManager(this);

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
                            double x = ((e.getX() - (scale / 2)) / scale) + xPosition;
                            double y = ((e.getY() - (scale / 2)) / scale) + yPosition;
                            // Snap coordinates
                            Point2D.Double snapped;
                            //TODO: this NEEDS to be removed
                            if (!creatingComponentID.equals("wirenode")) snapped = snapToGrid(x, y);
                            else snapped = snapToGrid(x, y, .1);
                            createNewComponent(snapped.x, snapped.y);

                        } catch (Exception ignored) {}
                    }
 
 
                    /* handles clicks on wires, checks for mouse clicks to the closest wire and
                       determines if it falls within the click threshold */
                    if (e.getButton()==1) {
                        selectedArea.clearMultiSelected();
                        boolean foundWire = false;
                        for (Wire wire : wires) {
                            if (wire.isNear(screenToWorld(e.getX(), e.getY())) && !foundWire) {
                                wire.setFocus();
                                foundWire = true;
                                continue;
                            }
                            wire.loseFocus();
                        }
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
                if (e.getKeyCode() == 27) {//escape key
                    if (inWireMode) {
                        inWireMode = false;
                        wireStartComponent = null;
                    }
                    creatingNewComponent = false;
                    creatingComponentID = null;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }

                // remove selected wire if delete key is pressed (if one is selected)
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode()==KeyEvent.VK_BACK_SPACE) { //delete key
                    deleteSelectedWires();
                    for (Wire rmWire : wires.stream().filter(Wire::isHighlighted).toList()) {
                        wires.remove(rmWire);
                        rmWire.endComponent.disconnect(rmWire.startComponent);
                        history.addEvent(History.Event.DELETED_WIRE, rmWire.startIndex, rmWire.endIndex, rmWire);
                        repaint();
                    }
                    for (Component c : getComponents()) {
                        if (c instanceof DraggableEditorComponent dec) {
                            if (dec.isMultiSelected) {
                                deleteComponent(dec.getElectricalComponent());
                            }
                        }
                    }

                }

                if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
                    DEBUG_NATIVE_DRAW_SIZE += .01F;
                    DEBUG_NATIVE_DRAW_SIZE = DEBUG_NATIVE_DRAW_SIZE>.04?.04F:DEBUG_NATIVE_DRAW_SIZE;
                    ComponentRenderer.clearBuffer();
                    repaint();
                }
                if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                    DEBUG_NATIVE_DRAW_SIZE -= .01F;
                    DEBUG_NATIVE_DRAW_SIZE = DEBUG_NATIVE_DRAW_SIZE<.01?.01F:DEBUG_NATIVE_DRAW_SIZE;
                    ComponentRenderer.clearBuffer();
                    repaint();
                }

                //if ctrl (17) held do wire mode
                if (e.getKeyCode() == 17) {
                    inWireMode = true;
                    selectedArea.clearMultiSelected();
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SLASH) {
                    debugDrawMode = !debugDrawMode;
                    repaint();
                }

                //for quickEntry field
                if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
                    if (e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z) {
                        Point mouse = MouseInfo.getPointerInfo().getLocation();
                        SwingUtilities.convertPointFromScreen(mouse, EditorArea.this);

                        quickEntry.setBounds(mouse.x, mouse.y,
                                quickEntry.getPreferredSize().width,
                                quickEntry.getPreferredSize().height);

                        quickEntry.setText(e.getKeyChar() + "");
                        quickEntry.setCaretPosition(1);
                        quickEntry.grabFocus();

                        quickEntry.setVisible(true);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == 17) {
                    inWireMode = false;
                    wireStartComponent = null;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                /* Check if mouse movement passed the threshold to be considered a drag and not a click,
                    prevents slight movement right before a click registering as a drag */
                if (!isDragging && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    int dx = Math.abs(e.getX() - pressScreenX);
                    int dy = Math.abs(e.getY() - pressScreenY);
                    if (dx >= DRAG_THRESHOLD || dy >= DRAG_THRESHOLD) {
                        isDragging = true;
                        quickEntry.setVisible(false);
                    } else {
                        return;
                    }
                }

                // update drag+canvas position variables
                if (isDragging) {
                    xDrag = (int) (xPosition + e.getX());
                    yDrag = (int) (yPosition + e.getY());
                    xPosition = lastReleasedPositionX - (xDrag - pressScreenX) / scale;
                    yPosition = lastReleasedPositionY - (yDrag - pressScreenY) / scale;

                    repaint();
                }
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
                currentFocusedComponent = null;
                for (Component c : getComponents()) {
                    if (c instanceof DraggableEditorComponent component) {
                        component.isMultiSelected = false;
                    }
                }
                repaint();
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
            quickEntry.setVisible(false);
            selectedArea.isDragging = false;

            //handle mouse wheel scroll on canvas
            double oldScale = scale;
            double zoomFactor = e.getPreciseWheelRotation() < 0 ? 1.1 : 1 / 1.1;
            scale*=zoomFactor;
            scale = ((int) scale);
            if (scale < 10) scale = 10; // lock scroll to positive values
            if (scale >= 1200) scale = 1200;

            //I forget what this does. Its probably important tho
            //      - 8/29/25 im pretty sure it centers the zoom
            Point mouse = e.getPoint();
            double mouseWorldXBefore = (mouse.x / oldScale) + xPosition;
            double mouseWorldYBefore = (mouse.y / oldScale) + yPosition;

            double mouseWorldXAfter = (mouse.x / scale) + xPosition;
            double mouseWorldYAfter = (mouse.y / scale) + yPosition;

            xPosition += (mouseWorldXBefore - mouseWorldXAfter);
            yPosition += (mouseWorldYBefore - mouseWorldYAfter);

            lastReleasedPositionX = xPosition;
            lastReleasedPositionY = yPosition;
            clearBuffer(); // regenerate components with new scale
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

    /**
     * Fits all components onto the screen and centers the zoom
     * @apiNote Method Repaints EditorArea
     */
    public void zoomFit(){
        double top = Double.MAX_VALUE;
        double bottom = Double.MIN_VALUE;
        double left = Double.MAX_VALUE;
        double right = Double.MIN_VALUE;
        if (getComponents().length == 0) return;
        for (Component c : getComponents()) {
            if (c instanceof DraggableEditorComponent) {
                DraggableEditorComponent dec = ((DraggableEditorComponent)c);
                double cx = dec.getWorldX();
                double cy = dec.getWorldY();
                left = Math.min(left, cx);
                right = Math.max(right, cx+dec.getWidth()/ scale);
                top = Math.min(top, cy);
                bottom = Math.max(bottom, cy+(dec.getHeight()/ scale));
            }
        }
        if (left==right && bottom==top) {
            xPosition = left - (getWidth() / (2.0 * scale));
            yPosition = top - (getHeight() / (2.0 * scale));

            lastReleasedPositionY = yPosition;
            lastReleasedPositionX = xPosition;
        }
        double worldWidth = (right - left) * 1.05;
        double worldHeight = (bottom - top)* 1.05;

        double scaleX = (getWidth()) / worldWidth;
        double scaleY = (getHeight()) / worldHeight;
        scale = Math.min(scaleX, scaleY);

        double worldCenterX = (left + right) / 2.0;
        double worldCenterY = (top + bottom) / 2.0;

        xPosition = worldCenterX - (getWidth() / (2.0 * scale));
        yPosition = worldCenterY - (getHeight() / (2.0 * scale));

        lastReleasedPositionY = yPosition;
        lastReleasedPositionX = xPosition;
        repaint();
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

    /**
     * Connects two ElectricalComponents with a wire and adds them to each other's children nodelist
     * @param a start component
     * @param aIndex component a's wire index to connect to
     * @param b end component
     * @param bIndex component b's wire index to connect to
     * @apiNote This operation may be logged in History (based on param addHistory)
     */
    public void connectComponents(ElectricalComponent a, int aIndex, ElectricalComponent b, int bIndex, boolean addHistory) {
        Wire w = new Wire(this, a, aIndex, b, bIndex);
        wires.add(w);
        a.connect(b);
        if (addHistory) history.addEvent(History.Event.CREATED_WIRE, w.startIndex, w.endIndex, w);
        repaint();
    }

    public void connectComponents(ElectricalComponent a, int aIndex, ElectricalComponent b, int bIndex) {
        connectComponents(a, aIndex, b, bIndex, true);
    }

    /** Enables wire placement mode in the editor (to place one singular wire */
    public void enableWireMode() {
        inWireMode = true;
        creatingNewComponent = false;
        creatingComponentID = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        grabFocus();
    }

    /**
     * Removes a component based off of its ElectricalComponent
     * @param eC the ElectricalComponent to be deleted
     * @apiNote This operation is logged in History
     */
    public void deleteComponent(ElectricalComponent eC) {
        grabFocus();
        history.addEvent(History.Event.DELETED_COMPONENT, eC.x, eC.y, eC);
        remove(eC.getDraggableEditorComponent());
        eC.setDeleted(true);
        ElectricalComponent.allComponents.remove(eC);
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

    /**
     * Set the editor to start creating a new component. This component will be locked to the mouse pointer until
     * the user clicks, when it will be actually created
     * @param id the id from ElectricalComponentIdentifier enum that corresponds to the component
     */
    public void setCreatingNewComponent(String id) {
        grabFocus();
        inWireMode = false;
        wireStartComponent = null;
        creatingNewComponent = true;
        creatingComponentID = id;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        creatingNewComponentImage = new ImageIcon("resources/"+id+".png").getImage();
    }

    /**
     * TODO: This shits as fucked as it looks. Should find a better way to do this
     * @param worldX x coordinate in world units
     * @param worldY y coordinate in world units
     * @param addHistory if true is logged to history, and therefore can be undone,
     *                   leave false for creations like loading from a file or internal editor calls
     * @apiNote This operation may be logged in History (based on addHistory param)
     */
    public DraggableEditorComponent createNewComponent(double worldX, double worldY, boolean addHistory) {
        Class compClass = ElectricalComponentIdentifier.findClassFromID(creatingComponentID);
        ElectricalComponent component = null;
        try {
            component = (ElectricalComponent) compClass.getDeclaredConstructor(EditorArea.class, double.class, double.class)
                    .newInstance(this, worldX, worldY);
        } catch (Exception e) {
            System.err.println("ERROR IN CREATING COMPONENT");
        }
        if (addHistory) history.addEvent(History.Event.CREATED_NEW_COMPONENT, worldX, worldY, component);
        return component.getDraggableEditorComponent();
    }

    public DraggableEditorComponent createNewComponent(double worldX, double worldY) throws
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return createNewComponent(worldX, worldY, true);
    }

    public DraggableEditorComponent createNewComponent(String compId, double x, double y) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return createNewComponent(compId, x, y, true);
    }

    public DraggableEditorComponent createNewComponent(String compId, double x, double y, boolean addHistory) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.creatingComponentID = compId;
        DraggableEditorComponent c = createNewComponent(x, y, addHistory);
        this.creatingComponentID = null;
        return c;
    }

    /**
     * Undo the last movement, deletion, or creation done in the editor from History
     * @apiNote This operation is logged in History
     */
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
                        ElectricalComponent.allComponents.add(component);
                        add(component.getDraggableEditorComponent());
                        wires.addAll(removedWires);
                    }
                } while (trackingEvent == History.Event.DELETED_WIRE);
                break;

            case History.Event.DELETED_COMPONENT:
                ElectricalComponent component = (ElectricalComponent) lastEvent.component;
                component.setDeleted(false);
                ElectricalComponent.allComponents.add(component);
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
                ElectricalComponent.allComponents.remove(component1);
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

    /**
     * Redo the last movement, deletion, or creation done in the editor from History
     * @apiNote This operation is logged in History
     */
    public void redo() {
        HistoryEntry lastEvent = history.getFutureAndRemove();
        if (lastEvent == null) return;

        switch (lastEvent.event) {

            case History.Event.DELETED_CONNECTING_WIRES:
                ElectricalComponent deletedComp = (ElectricalComponent) lastEvent.component;
                deletedComp.setDeleted(true);
                remove(deletedComp.getDraggableEditorComponent());

                List<Wire> removedWires = wires.stream()
                        .filter(w -> w.endComponent == deletedComp || w.startComponent == deletedComp)
                        .toList();
                for (Wire w : removedWires) {
                    wires.remove(w);
                    w.endComponent.disconnect(w.startComponent);
                }
                break;

            case History.Event.DELETED_COMPONENT:
                ElectricalComponent comp = (ElectricalComponent) lastEvent.component;
                comp.setDeleted(true);
                remove(comp.getDraggableEditorComponent());
                ElectricalComponent.allComponents.remove(comp);
                break;

            case History.Event.DELETED_WIRE:
                Wire wire = (Wire) lastEvent.component;
                wires.remove(wire);
                wire.endComponent.disconnect(wire.startComponent);
                break;

            case History.Event.CREATED_NEW_COMPONENT:
                ElectricalComponent newComp = (ElectricalComponent) lastEvent.component;
                newComp.setDeleted(false);
                ElectricalComponent.allComponents.add(newComp);
                add(newComp.getDraggableEditorComponent());
                break;

            case History.Event.CREATED_WIRE:
                Wire newWire = (Wire) lastEvent.component;
                wires.add(newWire);
                newWire.endComponent.connect(newWire.startComponent);
                break;

            case History.Event.MOVED_COMPONENT:
                ElectricalComponent movedComp = (ElectricalComponent) lastEvent.component;
                movedComp.getDraggableEditorComponent()
                        .setWorldPosition(lastEvent.editLocationX, lastEvent.editLocationY);
                break;

            case History.Event.ROTATED_COMPONENT:
                DraggableEditorComponent rotated = ((ElectricalComponent) lastEvent.component)
                        .getDraggableEditorComponent();
                rotated.orientation = lastEvent.rotation;
                ((ElectricalComponent) lastEvent.component).rotateConnectionPoints(lastEvent.rotation);
                break;
        }

        editorHistoryList.addEntry("REDO " + lastEvent.event + " " + lastEvent.componentType.getCanonicalName());
        repaint();
    }



    // ---------------------- // Draw all graphics // ---------------------- //

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // --- Ensure buffer exists and is valid ---
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.flush();
            GraphicsConfiguration gc = getGraphicsConfiguration();
            buffer = gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
        }

        do {
            int valid = buffer.validate(getGraphicsConfiguration());
            if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
                buffer.flush();
                buffer = getGraphicsConfiguration().createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
            }

            Graphics2D g2d = buffer.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ---- Clear background ----
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, width, height);

            // ---- FPS update ----
            long currentTime = System.nanoTime();
            double deltaSeconds = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            double currentFPS = 1.0 / deltaSeconds;
            fps = smoothing * fps + (1 - smoothing) * currentFPS;

            // ---- Grid drawing ----
            double worldLeft   = xPosition;
            double worldTop    = yPosition;
            double worldRight  = xPosition + width  / scale;
            double worldBottom = yPosition + height / scale;

            int startX = (int) Math.floor(worldLeft);
            int endX = (int) Math.ceil(worldRight);
            int startY = (int) Math.floor(worldTop);
            int endY = (int) Math.ceil(worldBottom);

            for (double gx = startX; gx <= endX; gx += 0.1) {
                int screenX = (int) ((gx - xPosition) * scale);
                int alpha = calcAlpha(scale, minScaleForGridLinesAppearing, maxScaleForGridLinesAppearing);

                if (Math.abs(gx - Math.round(gx)) < tolerance) {
                    g2d.setColor(Color.LIGHT_GRAY);
                } else {
                    g2d.setColor(new Color(204, 204, 204, alpha / 2));
                }
                g2d.drawLine(screenX, 0, screenX, height);
            }

            for (double gy = startY; gy <= endY; gy += 0.1) {
                int screenY = (int) ((gy - yPosition) * scale);
                int alpha = calcAlpha(scale, minScaleForGridLinesAppearing, maxScaleForGridLinesAppearing);

                if (Math.abs(gy - Math.round(gy)) < tolerance) {
                    g2d.setColor(Color.LIGHT_GRAY);
                } else {
                    g2d.setColor(new Color(204, 204, 204, alpha / 2));
                }
                g2d.drawLine(0, screenY, width, screenY);
            }

            // ---- Draw components ----
            for (Component c : getComponents()) {
                if (c instanceof DraggableEditorComponent) {
                    ((DraggableEditorComponent) c).updateFromEditor();
                }
            }

            // ---- Ghost new component ----
            g2d.setColor(Color.lightGray);
            if (creatingNewComponent) {
                Point mouse = getMousePosition();
                if (mouse != null) {
                    ComponentRenderer.renderDirect(null, g2d,mouse.x,mouse.y,
                            (int)(scale),creatingComponentID);
                }
            }

            // ---- Wires ----
            g2d.setColor(Color.BLACK);
            for (Wire wire : wires) {
                wire.draw(g2d, this);
            }

            // ---- Selection ----
            selectedArea.paint(g2d);

            if (debugDrawMode) debugGraphics(g2d);

            g2d.dispose();

        } while (buffer.contentsLost());

        // --- Blit final buffer to screen ---
        g.drawImage(buffer, 0, 0, null);
        Toolkit.getDefaultToolkit().sync();
    }

    private void debugGraphics(Graphics2D g2d) {
        g2d.setColor(Color.red);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.format("%.1f FPS", fps), 10, getHeight() - 10);
        g2d.drawString(String.format("%s BUFFER OBJECTS", ComponentRenderer.buffer.size()), 10, getHeight() - 30);
        g2d.drawString(String.format("%s TOTAL OBJECTS", ElectricalComponent.allComponents.size()),
                10, getHeight() - 50);
        g2d.drawString(String.format("%s TOTAL WIRES", wires.size()),
                10, getHeight() - 70);

        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Component c : getComponents()) {
            if (c instanceof DraggableEditorComponent dec) {
                Rectangle bounds = c.getBounds();
                g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

                Point2D.Double[] connectorPoints = dec.getElectricalComponent().getConnectionPoints();
                for (int i = 0; i < connectorPoints.length; i++) {
                    Point2D point = worldToScreen(dec.getWorldX()+connectorPoints[i].x,
                            (int) dec.getWorldY()+connectorPoints[i].y);
                    g2d.drawOval((int) (point.getX()-(.1*scale)/2), (int) (point.getY()-(.1*scale)/2),
                            (int) (.1*scale), (int) (.1*scale));
                }
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Wire w : wires) {
            Point2D.Double startWorld = w.startComponent.getConnectionPointsAsWorldPoints().get(w.startIndex);
            Point2D.Double endWorld = w.endComponent.getConnectionPointsAsWorldPoints().get(w.endIndex);

            Point startScreen = worldToScreen(startWorld.x, startWorld.y);
            Point endScreen = worldToScreen(endWorld.x, endWorld.y);
            Shape path = new Line2D.Double(startScreen.x, startScreen.y, endScreen.x, endScreen.y);

            BasicStroke outer = new BasicStroke((float) (.1*scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            BasicStroke inner = new BasicStroke((float) (scale * .06), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); // effectively the original path width

            Area strokeArea = new Area(outer.createStrokedShape(path));
            strokeArea.subtract(new Area(inner.createStrokedShape(path)));

            g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(strokeArea);
        }
        if (currentFocusedComponent != null) {
            String out = currentFocusedComponent.getElectricalComponent().electricalProperties.toString()
                    .replace("{","")
                    .replace("}","");
            String[] splitout = out.split(", ");
            for  (int i = 0; i < splitout.length; i++) {
                g2d.drawString(splitout[i],
                        10, 20+i*20);

            }
        }

    }


    /**
     * Helper for getting the transparency channel on the lines being drawn in the editor
     * @param scale the current editor scale
     * @param minScale the scale that the lines should appear at
     * @param maxScale the scale at which the lines are fully visible
     * @return int of alpha value
     */
    private int calcAlpha(double scale, float minScale, float maxScale) {
        if (scale <= minScale) return 0;             // fully transparent
        if (scale >= maxScale) return 255;           // fully opaque
        return (int) (255 * (scale - minScale) / (maxScale - minScale));
    }


    // ---------------------- // Getter+Setter Methods // ---------------------- //

    /**
     * Checks to see if the editor is currently in wire mode
     * @return boolean inWireMode
     */
    public boolean getInWireMode() {
        return inWireMode;
    }

    public EditorComponentInformationConfigurator getInformationConfigurator() {
        return informationConfigurator;
    }

    /**
     * Setter for the InformationConfigurator to be used in the editor
     * @param iC InformationConfigurator to be used
     */
    public void setInformationConfigurator(EditorComponentInformationConfigurator iC) {
        informationConfigurator = iC;
    }

    /**
     * Set Editor History Tracker to be used in this editor
     * @param editorHistoryList tracker to be used in the editor
     */
    public void setEditorHistoryList(EditorHistoryTrackerList editorHistoryList) {
        this.editorHistoryList =editorHistoryList;
        history.setEditorHistoryList(editorHistoryList);
    }

    /**
     * Getter for the currently selected component
     * @return DraggableEditorComponent that has focus in this EditorArea
     */
    public DraggableEditorComponent getFocusedComponent() {
        return currentFocusedComponent;
    }

    /**
     * Deletes any currently selected wire
     * @apiNote Method Repaints EditorArea
     *          This operation is logged in History
     */
    public void deleteSelectedWires() {
        Optional<Wire> removedWire = wires.stream().filter(w -> w.isHighlighted).findAny();
        if (removedWire.isPresent()) {
            Wire rmWire = removedWire.get();
            wires.remove(rmWire);
            rmWire.endComponent.disconnect(rmWire.startComponent);
            history.addEvent(History.Event.DELETED_WIRE, rmWire.startIndex, rmWire.endIndex, rmWire);
            repaint();
        }
    }

    /**
     * Setter for linking taskbar
     * @param taskbar taskbar to be linked to the editor
     */
    public void setBottomTaskbar(EditorBottomTaskbar taskbar) {
        this.taskbar = taskbar;
    }

    /** Completely reset the editor window and remove all current wires and component */
    public void reset() {
        for (Component c : getComponents()) {
            if (c instanceof DraggableEditorComponent) {
                deleteComponent(((DraggableEditorComponent) c).getElectricalComponent());
            }
        }
        history.clear();
        editorHistoryList.removeAllItems();
    }

    /**
     * Snap world coordinates to nearest grid point
     * @param worldX X-coordinate in world space
     * @param worldY Y-coordinate in world space
     * @param gridSize Grid size in world units (default = 1.0)
     * @return Point2D.Double of snapped coordinates
     */
    public Point2D.Double snapToGrid(double worldX, double worldY, double gridSize) {
        double snappedX = Math.round(worldX / gridSize) * gridSize;
        double snappedY = Math.round(worldY / gridSize) * gridSize;
        return new Point2D.Double(snappedX, snappedY);
    }

    /**
     * Snap world coordinates to nearest grid point
     * @param worldX X-coordinate in world space
     * @param worldY Y-coordinate in world space
     * @return Point2D.Double of snapped coordinates
     */
    public Point2D.Double snapToGrid(double worldX, double worldY) {
        return snapToGrid(worldX, worldY, 1.0);
    }

    /** TODO: METHOD IS GOING TO BE USED IN ELECTRICAL SIMULATION */
    public void highlightBestPath() {
        Stream<ElectricalComponent> powerSupplies = ElectricalComponent.allComponents.stream().filter(e -> e instanceof PowerSupply);
        Stream<ElectricalComponent> grounds = ElectricalComponent.allComponents.stream().filter(e -> e instanceof Ground);

        for (ElectricalComponent p : powerSupplies.toList()) {
            for (ElectricalComponent g : grounds.toList()) {
                highlight(p, g);
            }
        }
    }

    /** TODO: METHOD IS GOING TO BE USED IN ELECTRICAL SIMULATION */
    private void highlight(ElectricalComponent start, ElectricalComponent end) {
        java.util.List<ElectricalComponent> bestPath = ElectricalComponent.findPathOfLeastResistance(start, end);

        for (ElectricalComponent c : bestPath) {
            c.getDraggableEditorComponent().setBackground(new Color(144, 238, 144)); // light green
            c.getDraggableEditorComponent().setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        }

        repaint();
    }

}
 