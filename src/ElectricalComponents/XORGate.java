package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class XORGate extends ElectricalComponent {

    public XORGate(EditorArea ea, double x, double y) {
        super(ea, "xor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.1, .33));
        pins.add(new Point2D.Double(0.1, .67));
        pins.add(new Point2D.Double(0.9, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addCheckboxToInfoCard("Inverted Output");
        addCheckboxToInfoCard("Enable Propagation Delay");
        addEntryToInfoCard("Propagation Delay (ns)", 5);
        addEntryToInfoCard("Fan-In Limit", 4);
    }

}
