package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Speaker extends ElectricalComponent {
    public final static String id = "speaker";

    public Speaker(EditorArea ea, double x, double y) {
        super(ea, "speaker", x, y);

        shortenedId = "SP";
        draggableEditorComponent.boundsOverride = 1;

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(.23, .626));
        pins.add(new Point2D.Double(.23, .37));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() { // Power Supply
        styleInfoCard();
        addEntryToInfoCard("Voltage (V)", 6);
        addEntryToInfoCard("Current Limit (mA)", 6);
        addEntryToInfoCard("Ripple Voltage (mV)", 6);
        addEntryToInfoCard("Internal Resistance (Ω)", 6);
        addCheckboxToInfoCard("Enable Noise Simulation");
    }

}
