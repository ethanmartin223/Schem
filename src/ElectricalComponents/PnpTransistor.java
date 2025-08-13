package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PnpTransistor extends ElectricalComponent {

    public PnpTransistor(EditorArea ea, double x, double y) {
        super(ea, "pnptransistor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.628, .08));
        pins.add(new Point2D.Double(0.628, .92));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

}
