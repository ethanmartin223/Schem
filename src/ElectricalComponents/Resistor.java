package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Resistor extends ElectricalComponent {

    public Resistor(EditorArea ea, double x, double y) {
        super(ea, "resistor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Resistance", 10);
        addEntryToInfoCard("Tolerance", 10);
        addDropdownToInfoCard("Resistor Type", new String[] {"Ceramic", "Carbon", "Foil"});
        addCheckboxToInfoCard("Is Shorted");
        addCheckboxToInfoCard("Is Open");
    }
}
