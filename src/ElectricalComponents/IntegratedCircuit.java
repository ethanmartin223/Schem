
package ElectricalComponents;

import Editor.ComponentRenderer;
import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class IntegratedCircuit extends ElectricalComponent {
    public final static String id = "ic";
    public IntegratedCircuit(EditorArea ea, double x, double y) {
        super(ea, "ic", x, y);
        this.draggableEditorComponent.boundsOverride = 2.0;
        electricalProperties.put("number_of_pins", 16);
        recalculate_pin_points();
    }


    public void recalculate_pin_points() {
        if (electricalProperties.get("number_of_pins")==null) return;
        int numberOfPins = (int) electricalProperties.get("number_of_pins");

        double size = this.draggableEditorComponent.boundsOverride;

        double bodyWidth = size;
        double bodyHeight = (numberOfPins / 2.0) * (size * 0.2);
        bodyHeight = Math.max(bodyHeight, size * 0.8);

        double leadLength = size * 0.2;
        double leadSpacing = bodyHeight / (numberOfPins / 2.0);

        double cx = x;
        double cy = y;

        ArrayList<Point2D.Double> pins = new ArrayList<>();
        for (int i = 0; i < numberOfPins / 2; i++) {
            double py = cy - bodyHeight / 2 + (i + 0.5) * leadSpacing;
            double px = cx - bodyWidth / 2 - leadLength;
            pins.add(new Point2D.Double(px, py));
        }
        for (int i = 0; i < numberOfPins / 2; i++) {
            double py = cy + bodyHeight / 2 - (i + 0.5) * leadSpacing;
            double px = cx + bodyWidth / 2 + leadLength;
            pins.add(new Point2D.Double(px, py));
        }

        setConnectionPoints(pins);
    }


    @Override
    protected void onPropertiesChange() {
        super.onPropertiesChange();
        recalculate_pin_points();
        ComponentRenderer.clearBuffer();
    }

    @Override
    public boolean isIndividuallyRendered() {
        return true;
    }

    @Override
    public void initInfoCard() {
        styleInfoCard();
        addEntryToInfoCard("number_of_pins", 6);
        addCheckboxToInfoCard("labeled_pins");
    }
}
