package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Transformer extends ElectricalComponent {

    public Transformer(EditorArea ea, double x, double y) {
        super(ea, "transformer", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.8, .2));
        pins.add(new Point2D.Double(0.2, .2));
        pins.add(new Point2D.Double(0.2, .8));
        pins.add(new Point2D.Double(0.8, .8));
        setConnectionPoints(pins);
    }

}
