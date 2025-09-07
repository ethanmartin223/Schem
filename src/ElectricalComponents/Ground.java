package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Ground extends ElectricalComponent {

    public Ground(EditorArea ea, double x, double y) {
        super(ea, "ground", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();

        pins.add(new Point2D.Double(0.5, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addCheckboxToInfoCard("Reference Enabled");
        addEntryToInfoCard("Ground Offset (mV)", 6);
    }

}