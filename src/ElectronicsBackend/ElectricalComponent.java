package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;
import ElectricalComponents.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ElectricalComponent {
    public String id;
    public double x, y;

    public Double hitBoxWidthOverride = null;
    public Double hitBoxHeightOverride = null;
    public String shortenedId = null;

    protected JPanel infoCard;
    private HashMap<String, Component> infoCardComponents;

    private Point2D.Double[] connectionPoints;
    private Point2D.Double[] internalConnectionPoints;

    protected EditorArea editorArea;
    protected DraggableEditorComponent draggableEditorComponent;
    public boolean isDeleted;

    //whenever you make a new component, just add it to the subclass list and add it to the findClassFromID function

    public static Class<?>[] subclasses = new Class[]{
            ANDGate.class,
            Capacitor.class,
            Diode.class,
            Ground.class,
            NANDGate.class,
            NpnTransistor.class,
            ORGate.class,
            PnpTransistor.class,
            PowerSupply.class,
            Resistor.class,
            Transformer.class,
            VariableResistor.class,
            XORGate.class,
            ZenerDiode.class,
            Speaker.class,
            Lamp.class,
            WireNode.class,
            Microphone.class,
            LED.class,
            Photoresistor.class,
            IntegratedCircuit.class,
            VoltageSupply.class
    };
    int idNum;

    public static Class<?> findClassFromID(String id) {
        switch (id) {
            case ANDGate.id ->   { return ANDGate.class; }
            case Capacitor.id -> { return Capacitor.class; }
            case Diode.id ->     { return Diode.class; }
            case Ground.id ->    { return Ground.class; }
            case NANDGate.id ->  { return NANDGate.class; }
            case NpnTransistor.id -> { return NpnTransistor.class; }
            case ORGate.id ->    { return ORGate.class; }
            case PnpTransistor.id -> { return PnpTransistor.class; }
            case PowerSupply.id -> { return PowerSupply.class; }
            case Resistor.id -> { return Resistor.class; }
            case Transformer.id -> { return Transformer.class; }
            case VariableResistor.id -> { return VariableResistor.class; }
            case XORGate.id -> { return XORGate.class; }
            case ZenerDiode.id -> { return ZenerDiode.class; }
            case Speaker.id -> { return Speaker.class; }
            case Lamp.id -> { return Lamp.class; }
            case WireNode.id -> { return WireNode.class; }
            case Microphone.id -> { return Microphone.class; }
            case LED.id -> { return LED.class; }
            case Photoresistor.id -> { return Photoresistor.class; }
            case IntegratedCircuit.id -> { return IntegratedCircuit.class; }
            case VoltageSupply.id -> { return VoltageSupply.class; }
            default -> {
                return null;
            }
        }
    }

    // ----------------------------------------------------- comp shit
    public static ArrayList<ElectricalComponent> allComponents = new ArrayList<>();

    private int resistance;
    public ArrayList<ElectricalComponent> children;
    public HashMap<String, Object> electricalProperties;

    public void connect(ElectricalComponent other) {
        if (other == this) return;
        this.children.add(other);
        other.children.add(this);
    }

    public void disconnect(ElectricalComponent other) {
        if (other == this) return;
        this.children.remove(other);
        other.children.remove(this);
    }

    public int getResistance() {
        Object c = electricalProperties.get("Resistance (Ω)");
        if (c!= null) return (int)c;
        else return 0;
    }

    public ArrayList<ElectricalComponent> getChildren() {
        return children;
    }

    public static void printConnectionMap() {
        for (ElectricalComponent c : allComponents) {
            System.out.println(c + " -> " + c.children);
        }
        System.out.println();
    }



    public static List<ElectricalComponent> findPathOfLeastResistance(ElectricalComponent start, ElectricalComponent end) {
        Map<ElectricalComponent, Integer> dist = new HashMap<>();
        Map<ElectricalComponent, ElectricalComponent> prev = new HashMap<>();
        PriorityQueue<ElectricalComponent> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (ElectricalComponent c : allComponents) {
            dist.put(c, Integer.MAX_VALUE);
            prev.put(c, null);
        }
        dist.put(start, 0);
        pq.add(start);
        while (!pq.isEmpty()) {
            ElectricalComponent u = pq.poll();
            if (u == end) break;
            for (ElectricalComponent v : u.getChildren()) {
                int alt = dist.get(u) + v.getResistance();
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        List<ElectricalComponent> path = new ArrayList<>();
        ElectricalComponent step = end;
        if (prev.get(step) != null || step == start) {
            while (step != null) {
                path.add(0, step);
                step = prev.get(step);
            }
        }
        return path;
    }

    public static List<List<ElectricalComponent>> findAllPaths(ElectricalComponent start, ElectricalComponent end) {
        List<List<ElectricalComponent>> allPaths = new ArrayList<>();
        Queue<List<ElectricalComponent>> queue = new LinkedList<>();

        List<ElectricalComponent> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.add(initialPath);

        while (!queue.isEmpty()) {
            List<ElectricalComponent> path = queue.poll();
            ElectricalComponent last = path.get(path.size() - 1);

            if (last.equals(end)) {
                allPaths.add(new ArrayList<>(path));
            } else {
                for (ElectricalComponent neighbor : last.getChildren()) {
                    if (!path.contains(neighbor)) {
                        List<ElectricalComponent> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        queue.add(newPath);
                    }
                }
            }
        }
        return allPaths;
    }


    public static String[] compListToCharList(List<ElectricalComponent> compList) {
        String[] output = new String[compList.size()];
        for (int i = 0; i < compList.size(); i++) {
            output[i] = compList.get(i).id;
        }
        return output;
    }

    //------------------------------------------- end comp shit

    public ElectricalComponent(EditorArea eA, String stringId, double worldX, double worldY) {
        id = stringId;

        x = worldX;
        y = worldY;

        //link component stuff
        this.resistance = -1;
        this.children = new ArrayList<>();

        isDeleted = false;
        editorArea = eA;

        electricalProperties = new HashMap<>();
        infoCardComponents = new HashMap<>();
        infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        draggableEditorComponent = new DraggableEditorComponent(eA, x, y, this);
        editorArea.add(draggableEditorComponent);

        allComponents.add(this);
        idNum = allComponents.indexOf(this);

        initInfoCard();

    }

    public void setConnectionPoints(ArrayList<Point2D.Double> points) {
        connectionPoints = new Point2D.Double[points.size()];
        internalConnectionPoints = new Point2D.Double[points.size()];

        for (int i = 0; i < connectionPoints.length; i++) {
            internalConnectionPoints[i] = (new Point2D.Double(points.get(i).getX(), points.get(i).getY()));
            connectionPoints[i] = (new Point2D.Double(points.get(i).getX(), points.get(i).getY()));
        }
    }

    public void rotateConnectionPoints(int direction) {
        for (int i = 0; i < connectionPoints.length; i++) {
            if (direction == 0) {
                connectionPoints[i] = new Point2D.Double(internalConnectionPoints[i].getX(),
                        internalConnectionPoints[i].getY());
            } else if (direction == 1) {
                connectionPoints[i] = new Point2D.Double(internalConnectionPoints[i].getY(),
                        1 - internalConnectionPoints[i].getX());
            } else if (direction == 2) {
                connectionPoints[i] = new Point2D.Double(1 - internalConnectionPoints[i].getX(),
                        1 - internalConnectionPoints[i].getY());
            } else if (direction == 3) {
                connectionPoints[i] = new Point2D.Double(1 - internalConnectionPoints[i].getY(),
                        internalConnectionPoints[i].getX());
            }
        }
    }

    public void initInfoCard() {
    }

    public ArrayList<Point2D.Double> getConnectionPointsAsWorldPoints() {
        ArrayList<Point2D.Double> worldPoints = new ArrayList<>();
        double baseX = draggableEditorComponent.getWorldX();
        double baseY = draggableEditorComponent.getWorldY();

        for (Point2D.Double local : connectionPoints) {
            worldPoints.add(new Point2D.Double(
                    (baseX + local.x),
                    (baseY + local.y)));
        }

        return worldPoints;
    }


    public DraggableEditorComponent getDraggableEditorComponent() {
        return draggableEditorComponent;
    }

    public JPanel getInfoCard() {
        return infoCard;
    }

    @Override
    public String toString() {

        int[] c = new int[children.size()];
        for (int i = 0; i < children.size(); i++) c[i] = allComponents.indexOf(children.get(i));

        return "ElectricalComponent(compId:" + idNum + "|type:" + id + "|x:" + x + "|y:" + y + "|isDead:" + isDeleted +
                "|rot:" + draggableEditorComponent.orientation + "|children:" + Arrays.toString(c).replace(" ", "") + ")";
    }

    private Integer convertNumberString(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void onPropertiesChange() {
        editorArea.repaint();
        System.out.println("onPropertiesChange Fired for " + this.toString());
    }

    public void setDeleted(boolean b) {
        isDeleted = b;
    }

    public Point2D.Double[] getConnectionPoints() {
        return connectionPoints;
    }

    // Override this if the component needs to be excluded from cached rendering
    public boolean isIndividuallyRendered() {
        return false;
    }

    // ---------- // INFO CARD METHODS // --------- //
    protected void styleInfoCard() {
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        infoCard.setBackground(new Color(245, 245, 245)); // light gray modern background
    }

    private JPanel createMenuItem(JComponent component, String labelText) {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(10, 0));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(60, 60, 60));

        if (component != null) {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            component.setPreferredSize(new Dimension(150, 25));
            container.add(component, BorderLayout.EAST);
        }
        container.add(label, BorderLayout.WEST);

        // subtle hover effect
        container.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                container.setBackground(new Color(235, 235, 235));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                container.setBackground(Color.WHITE);
            }
        });

        electricalProperties.put(labelText, null);
        return container;
    }

    protected void addDropdownToInfoCard(String key, String[] dropdownItems) {
        JComboBox<String> comboBox = new JComboBox<>(dropdownItems);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                electricalProperties.put(key, comboBox.getSelectedItem());
                onPropertiesChange();
            }
        });
        electricalProperties.put(key, comboBox.getSelectedItem());

        JPanel menuItem = createMenuItem(comboBox, key);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(menuItem);
        infoCardComponents.put(key, comboBox);
    }

    protected void addEntryToInfoCard(String key, int entryWidth) {
        JTextField textField = new JTextField(entryWidth);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel menuItem = createMenuItem(textField, key);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(menuItem);
        infoCardComponents.put(key, textField);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                electricalProperties.put(key, convertNumberString(textField.getText()));
                onPropertiesChange();
            }
        });
    }

    protected void addCheckboxToInfoCard(String key) {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(Color.WHITE);

        JPanel menuItem = createMenuItem(checkbox, key);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(menuItem);
        infoCardComponents.put(key, checkbox);

        checkbox.addItemListener(e -> {
            electricalProperties.put(key, e.getStateChange() == ItemEvent.SELECTED);
            onPropertiesChange();
        });
    }

    public void updateInfoPanelFromProperties() {
        for (String key :this.infoCardComponents.keySet()){
            if (electricalProperties.get(key) == null) {
                continue;
            } else if (this.infoCardComponents.get(key).getClass() == JTextField.class) {
                JTextField field = (JTextField)this.infoCardComponents.get(key);
                field.setText(""+electricalProperties.get(key));
            } else if (this.infoCardComponents.get(key).getClass() == JComboBox.class) {
                JComboBox comboBox = (JComboBox)this.infoCardComponents.get(key);
                comboBox.getModel().setSelectedItem(electricalProperties.get(key));

            } else if (this.infoCardComponents.get(key).getClass() == JCheckBox.class) {
                JCheckBox checkBox = (JCheckBox)this.infoCardComponents.get(key);
                checkBox.setSelected((Boolean) electricalProperties.get(key));
            }
        }
    }
}