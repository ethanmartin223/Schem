package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PowerSupply extends ElectricalComponent {
    public final static String id = "powerSupply";

    public PowerSupply(EditorArea ea, double x, double y) {
        super(ea, "powerSupply", x, y);

        shortenedId = "VCC";
        hitBoxHeightOverride = .65;
        hitBoxWidthOverride = .5;

        ArrayList<Point2D.Double> pins = new ArrayList<>();

        pins.add(new Point2D.Double(0.5, .8));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() { // Power Supply
        styleInfoCard();
        addEntryToInfoCard("Voltage (V)", 6);
        addEntryToInfoCard("Current (A)", 6);
        addEntryToInfoCard("Current Limit (mA)", 6);
        addEntryToInfoCard("Ripple Voltage (mV)", 6);
        addEntryToInfoCard("Internal Resistance (Ω)", 6);
        addCheckboxToInfoCard("Enable Noise Simulation");
    }


    public double getVoltage() {
        return Double.parseDouble(electricalProperties.get("Voltage (V)")+"");
    }

    public double getCurrent() {
        return Double.parseDouble(electricalProperties.get("Current (A)")+"");
    }
}
