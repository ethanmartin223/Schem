package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class ElectricalComponent {
    protected Image baseImage, selectedImage;
    protected String id;
    public double x, y;

    protected JPanel infoCard;
    private HashMap<String, Component> infoCardComponents;

    private Point2D.Double[] connectionPoints;
    private Point2D.Double[] internalConnectionPoints;

    protected EditorArea editorArea;
    protected DraggableEditorComponent draggableEditorComponent;
    private boolean isDeleted;

    public ElectricalComponent(EditorArea eA, String stringId, double worldX, double worldY) {
        id = stringId;
        baseImage = new ImageIcon("resources/"+stringId+".png").getImage();
        selectedImage = new ImageIcon("resources/"+stringId+"Selected.png").getImage();

        x = worldX;
        y = worldY;
        isDeleted = false;
        editorArea = eA;

        infoCardComponents = new HashMap<>();
        infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        draggableEditorComponent = new DraggableEditorComponent(eA, baseImage, selectedImage, x, y, this);
        editorArea.add(draggableEditorComponent);

        initInfoCard();
    }

    public void setConnectionPoints(ArrayList<Point2D.Double> points) {
        connectionPoints = new Point2D.Double[points.size()];
        internalConnectionPoints = new Point2D.Double[points.size()];

        for (int i= 0; i<connectionPoints.length; i++){
            internalConnectionPoints[i] = (new Point2D.Double(points.get(i).getX(), points.get(i).getY()));
            connectionPoints[i] = (new Point2D.Double(points.get(i).getX(), points.get(i).getY()));
        }
    }

    public void rotateConnectionPoints(int direction) {
        for (int i= 0; i<connectionPoints.length; i++){
            if (direction == 0) {
                connectionPoints[i] = new Point2D.Double(internalConnectionPoints[i].getX(),
                        internalConnectionPoints[i].getY());
            } else if (direction == 1) {
                connectionPoints[i] = new Point2D.Double(internalConnectionPoints[i].getY(),
                        1-internalConnectionPoints[i].getX());
            } else if (direction == 2) {
                connectionPoints[i] = new Point2D.Double(1-internalConnectionPoints[i].getX(),
                        1-internalConnectionPoints[i].getY());
            } else if (direction == 3) {
                connectionPoints[i] = new Point2D.Double(1-internalConnectionPoints[i].getY(),
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
            worldPoints.add(new Point2D.Double(baseX + local.x, baseY + local.y));
        }

        return worldPoints;
    }

    public DraggableEditorComponent getDraggableEditorComponent() {
        return draggableEditorComponent;
    }

    public JPanel getInfoCard() {
        return infoCard;
    }













    // ---------- // INFO CARD METHODS // --------- //
    protected void styleInfoCard() {
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JPanel createMenuItem(Component iconOrField, String labelText) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel label = new JLabel(labelText);

        container.add(label);
        if (iconOrField!=null)container.add(iconOrField);

        return container;
    }

    protected void addDropdownToInfoCard(String key, String[] dropdownItems) {
        JComboBox<String> comboBox = new JComboBox<>();
        for (String item : dropdownItems) {
            comboBox.addItem(item);
        }

        JPanel menuItem = createMenuItem(comboBox, key);
        infoCard.add(menuItem);
        infoCardComponents.put(key, comboBox);
    }

    protected void addEntryToInfoCard(String key, int entryWidth) {
        JTextField textField = new JTextField(entryWidth);

        JPanel menuItem = createMenuItem(textField, key);
        infoCard.add(menuItem);
        infoCardComponents.put(key, textField);
    }

    protected void addCheckboxToInfoCard(String key) {
        JCheckBox checkbox = new JCheckBox();

        JPanel menuItem = createMenuItem(checkbox, key);
        infoCard.add(menuItem);
        infoCardComponents.put(key, checkbox);
    }

    public void setDeleted(boolean b) {
        isDeleted = b;
    }
}
