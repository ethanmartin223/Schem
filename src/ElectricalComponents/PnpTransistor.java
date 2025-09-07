package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PnpTransistor extends ElectricalComponent {

    public PnpTransistor(EditorArea ea, double x, double y) {
        super(ea, "pnptransistor", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.6d, .1d));
        pins.add(new Point2D.Double(0.6d, .9d));
        pins.add(new Point2D.Double(0.1d, .5d));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Base Bias (V)", 6);
        addEntryToInfoCard("Emitter Current (mA)", 6);
        addEntryToInfoCard("Gain (hFE)", 6);
        addEntryToInfoCard("Leakage Current (μA)", 6);
        addDropdownToInfoCard("Mode", new String[] {"Cutoff", "Active", "Saturation"});
        addCheckboxToInfoCard("Thermal Runaway Protection");
        addCheckboxToInfoCard("Is Shorted");
        addCheckboxToInfoCard("Is Open");
    }


}
