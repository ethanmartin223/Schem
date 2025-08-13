package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ANDGate extends ElectricalComponent {

    public ANDGate(EditorArea ea, double x, double y) {
        super(ea, "and", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.1, .33));
        pins.add(new Point2D.Double(0.1, .67));
        pins.add(new Point2D.Double(0.9, .5));
        setConnectionPoints(pins);
    }

}
