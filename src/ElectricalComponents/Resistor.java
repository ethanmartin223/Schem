package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Resistor extends ElectricalComponent {

    public Resistor(EditorArea ea, double x, double y) {
        super(ea, "resistor", x, y);

        hitBoxHeightOverride = .5;
        hitBoxWidthOverride = .85;

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() { // Resistor
        styleInfoCard();
        addEntryToInfoCard("Resistance (Ω)", 8);
        addEntryToInfoCard("Tolerance (%)", 4);
        addEntryToInfoCard("Power Rating (W)", 5);
        addDropdownToInfoCard("Material", new String[] {"Carbon Film", "Metal Film", "Wirewound"});
        addCheckboxToInfoCard("Is Burnt Out");
    }

}
