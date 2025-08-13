package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Diode extends ElectricalComponent {

    public Diode(EditorArea ea, double x, double y) {
        super(ea, "diode", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Base Bias", 10);
        addCheckboxToInfoCard("Is Shorted");
        addCheckboxToInfoCard("Is Open");
    }
}
