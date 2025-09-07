package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Capacitor extends ElectricalComponent {

    public Capacitor(EditorArea ea, double x, double y) {
        super(ea, "capacitor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public boolean hasNativeDraw() {
        return true;
    }


}
