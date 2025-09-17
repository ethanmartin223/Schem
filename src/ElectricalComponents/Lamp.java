package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Lamp extends ElectricalComponent {
    public final static String id = "lamp";

    public Lamp(EditorArea ea, double x, double y) {
        super(ea, "lamp", x, y);

        hitBoxHeightOverride = .5;
        hitBoxWidthOverride = .85;

        shortenedId = "L";

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Rated Voltage (V)", 6);
        addEntryToInfoCard("Current Draw (mA)", 6);
        addEntryToInfoCard("Brightness (%)", 4);
        addCheckboxToInfoCard("Is Burnt Out");
        addCheckboxToInfoCard("Is Flickering");
    }

}
