package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class NpnTransistor extends ElectricalComponent {

    public NpnTransistor(EditorArea ea, double x, double y) {
        super(ea, "npntransistor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.628, .08));
        pins.add(new Point2D.Double(0.628, .92));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Base Bias (V)", 6);
        addEntryToInfoCard("Collector Current (mA)", 6);
        addEntryToInfoCard("Gain (hFE)", 6);
        addEntryToInfoCard("Saturation Voltage (V)", 6);
        addDropdownToInfoCard("Mode", new String[] {"Cutoff", "Active", "Saturation"});
        addCheckboxToInfoCard("Thermal Runaway Protection");
        addCheckboxToInfoCard("Is Shorted");
        addCheckboxToInfoCard("Is Open");
    }

}
