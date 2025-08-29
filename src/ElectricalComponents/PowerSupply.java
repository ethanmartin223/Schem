package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PowerSupply extends ElectricalComponent {

    public PowerSupply(EditorArea ea, double x, double y) {
        super(ea, "powerSupply", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();

        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Supply Voltage", 5);
        addDropdownToInfoCard("Power Supply Type", new String[] {"AC", "DC"});
    }
}
