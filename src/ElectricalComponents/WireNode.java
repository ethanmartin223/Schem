package ElectricalComponents;

import Editor.EditorArea;
import ElectronicsBackend.ElectricalComponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class WireNode extends ElectricalComponent {
    public final static String id = "wirenode";
    public WireNode(EditorArea eA, double worldX, double worldY) {
        super(eA, "wirenode", worldX, worldY);
        ArrayList<Point2D.Double> pins = new ArrayList<>();
        pins.add(new Point2D.Double(0.5, .5));

        shortenedId = "";
        this.draggableEditorComponent.boundsOverride = .25;
        setConnectionPoints(pins);

    }
}
