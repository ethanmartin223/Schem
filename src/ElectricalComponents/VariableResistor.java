package ElectricalComponents;
import javax.swing.*;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class VariableResistor extends ElectricalComponent {

    public VariableResistor(EditorArea ea, double x, double y) {
        super(ea, "variableresistor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() { // Variable Resistor
        styleInfoCard();
        addEntryToInfoCard("Resistance (Ω)", 8);
        addEntryToInfoCard("Wiper Position (%)", 4);
        addEntryToInfoCard("Tolerance (%)", 4);
        addDropdownToInfoCard("Type", new String[]{"Potentiometer", "Rheostat", "Trimpot"});
    }


}
