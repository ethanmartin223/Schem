package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class LED extends ElectricalComponent {
    public final static String id = "led";

    public LED(EditorArea ea, double x, double y) {
        super(ea, "led", x, y);

        hitBoxHeightOverride = .85;
        hitBoxWidthOverride = .85;

        shortenedId = "LED";

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Response Time (ms)", 6);
        addCheckboxToInfoCard("Temperature Dependent");
    }

}
