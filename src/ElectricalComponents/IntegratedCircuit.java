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
        if (electricalProperties.get("number_of_pins") == null) return;
        int numberOfPins = (int) electricalProperties.get("number_of_pins");

        double size =1;

        double bodyWidth = size;
        double bodyHeight = (numberOfPins / 2.0) * (size * 0.2);
        bodyHeight = Math.max(bodyHeight, size * 0.8);

        double leadLength = size * 0.2;
        double leadSpacing = bodyHeight / (numberOfPins / 2.0);

        int rot = this.draggableEditorComponent.orientation;
        if  (rot==0 || rot==2)hitBoxHeightOverride = bodyHeight/this.draggableEditorComponent.boundsOverride+this.draggableEditorComponent.boundsOverride*.2;
        if  (rot==1 || rot==3)hitBoxWidthOverride = bodyHeight/this.draggableEditorComponent.boundsOverride+this.draggableEditorComponent.boundsOverride*.2;

        double cx = .5; // This must be in world coordinates
        double cy = .5;

        ArrayList<Point2D.Double> pins = new ArrayList<>();

        // Left side pins (pins 1 to N/2)
        for (int i = 0; i < numberOfPins / 2; i++) {
            double py = cy - bodyHeight / 2 + (i + 0.5) * leadSpacing;
            double px = cx - bodyWidth / 2 - leadLength;
            pins.add(new Point2D.Double(px, py));
        }

        // Right side pins (pins N to N/2+1 in reverse order)
        for (int i = 0; i < numberOfPins / 2; i++) {
            double py = cy + bodyHeight / 2 - (i + 0.5) * leadSpacing;
            double px = cx + bodyWidth / 2 + leadLength;
            pins.add(new Point2D.Double(px, py));
        }

        setConnectionPoints(pins);
        this.draggableEditorComponent.repaint();
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