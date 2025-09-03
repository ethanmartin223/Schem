package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Microphone extends ElectricalComponent {

    public Microphone(EditorArea ea, double x, double y) {
        super(ea, "microphone", x, y);

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.9, .5));
        pins.add(new Point2D.Double(0.1, .5));
        setConnectionPoints(pins);
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("Sensitivity (mV/Pa)", 6);
        addEntryToInfoCard("Impedance (Ω)", 8);
        addEntryToInfoCard("Frequency Response Low (Hz)", 6);
        addEntryToInfoCard("Frequency Response High (Hz)", 6);
        addCheckboxToInfoCard("Is Muted");
    }


}
