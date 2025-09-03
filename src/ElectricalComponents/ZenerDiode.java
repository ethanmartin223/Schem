package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ZenerDiode extends ElectricalComponent {

    public ZenerDiode(EditorArea ea, double x, double y) {
        super(ea, "zenerdiode", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Zener Voltage (V)", 6);
        addEntryToInfoCard("Max Current (mA)", 6);
        addEntryToInfoCard("Dynamic Resistance (Ω)", 6);
        addCheckboxToInfoCard("Temperature Compensation Enabled");
        addCheckboxToInfoCard("Is Shorted");
        addCheckboxToInfoCard("Is Open");
    }

}
