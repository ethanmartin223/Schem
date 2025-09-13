package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class NANDGate extends ElectricalComponent {

    public NANDGate(EditorArea ea, double x, double y) {
        super(ea, "nand", x, y);

        hitBoxHeightOverride = .6;
        hitBoxWidthOverride = .85;

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.1, .3));
        pins.add(new Point2D.Double(0.1, .7));
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
