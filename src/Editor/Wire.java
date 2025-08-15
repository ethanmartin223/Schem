package Editor;


import ElectronicsBackend.ElectricalComponent;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Wire {
    public static final Color HIGHLIGHT_COLOR = new Color(255,153,0);

    public ElectricalComponent startComponent;
    public ElectricalComponent endComponent;

    public boolean isHighlighted = false;

    public int startIndex, endIndex;

    public Wire(ElectricalComponent start, int startIndex, ElectricalComponent end, int endIndex) {
        this.startComponent = start;
        this.endComponent = end;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public void setFocus() {
        isHighlighted = true;
    }

    public void loseFocus() {
        isHighlighted = false;
    }

    public Wire deepCopy() {
        return new Wire(this.startComponent, this.startIndex, this.endComponent, this.endIndex);
    }

    public void draw(Graphics2D g, EditorArea editor) {
        Point2D.Double startWorld = startComponent.getConnectionPointsAsWorldPoints().get(startIndex);
        Point2D.Double endWorld = endComponent.getConnectionPointsAsWorldPoints().get(endIndex);

        Point startScreen = editor.worldToScreen(startWorld.x, startWorld.y);
        Point endScreen = editor.worldToScreen(endWorld.x, endWorld.y);

        if (isHighlighted) {
            for (int i = 5; i >= 1; i--) {
                float width = (float) (editor.scale * 0.03 * i);
                float alpha = Math.max(0.10f, 0.65f / i);
                g.setColor(new Color(
                        HIGHLIGHT_COLOR.getRed(),
                        HIGHLIGHT_COLOR.getGreen(),
                        HIGHLIGHT_COLOR.getBlue(),
                        Math.min(255, (int)(alpha * 255))
                ));
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(startScreen.x, startScreen.y, endScreen.x, endScreen.y);
            }
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke((float) (editor.scale * 0.040)));
        g.drawLine(startScreen.x, startScreen.y, endScreen.x, endScreen.y);
    }



    public boolean isNear(Point2D.Double point) {
        double threshold = 0.1; // tolerance in world coordinates

        Point2D.Double a = startComponent.getConnectionPointsAsWorldPoints().get(startIndex);
        Point2D.Double b = endComponent.getConnectionPointsAsWorldPoints().get(endIndex);

        return pointToSegmentDistance(point, a, b) < threshold;
    }


    private double pointToSegmentDistance(Point2D.Double p, Point2D.Double a, Point2D.Double b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        if (dx == 0 && dy == 0) {
            return p.distance(a);
        }

        double t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / (dx*dx + dy*dy);
        t = Math.max(0, Math.min(1, t));

        double projX = a.x + t * dx;
        double projY = a.y + t * dy;

        return p.distance(projX, projY);
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public ElectricalComponent getStartComponent() {
        return startComponent;
    }

    public ElectricalComponent getEndComponent() {
        return endComponent;
    }
}
