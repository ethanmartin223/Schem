package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class VoltageSupply extends ElectricalComponent {
    public final static String id = "voltageSupply";

    public VoltageSupply(EditorArea ea, double x, double y) {
        super(ea, "voltageSupply", x, y);

        hitBoxHeightOverride = .9;
        hitBoxWidthOverride = .8;

        shortenedId = "VCC";

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.5, .9));
        pins.add(new Point2D.Double(0.5, .1));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() { // Power Supply
        styleInfoCard();
        addEntryToInfoCard("Voltage (V)", 6);
        addEntryToInfoCard("Current Limit (mA)", 6);
        addEntryToInfoCard("Ripple Voltage (mV)", 6);
        addEntryToInfoCard("Resistance (Ω)", 6);
        addCheckboxToInfoCard("Enable Noise Simulation");
    }


}
