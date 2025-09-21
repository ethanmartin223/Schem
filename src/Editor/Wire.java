package Editor;


import ElectronicsBackend.ElectricalComponent;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Wire {
    public static final Color HIGHLIGHT_COLOR = new Color(255,153,0);

    public ElectricalComponent startComponent;
    public ElectricalComponent endComponent;

    public EditorArea editor;
    public boolean isHighlighted = false;

    public int startIndex, endIndex;

    public boolean isMultiSelected;

    public Wire(EditorArea parent, ElectricalComponent start, int startIndex, ElectricalComponent end, int endIndex) {
        this.startComponent = start;
        this.endComponent = end;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        editor = parent;

        isMultiSelected = false;
    }

    public void setFocus() {
        isHighlighted = true;
    }

    public void loseFocus() {
        isHighlighted = false;
    }

    public void draw(Graphics2D g2d, EditorArea editor) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Point2D.Double startWorld = startComponent.getConnectionPointsAsWorldPoints().get(startIndex);
        Point2D.Double endWorld   = endComponent.getConnectionPointsAsWorldPoints().get(endIndex);
        Point s = editor.worldToScreen(startWorld.x, startWorld.y);
        Point e = editor.worldToScreen(endWorld.x, endWorld.y);

        double ex = e.x;
        double ey = e.y;
        double sx = s.x;
        double sy = s.y;

        if (isHighlighted || isMultiSelected) {
            float width = (float) (editor.scale * 0.06);
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new java.awt.geom.Line2D.Double(sx, sy, ex, ey));
        }
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(
                (float) (editor.scale * EditorArea.DEBUG_NATIVE_DRAW_SIZE),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        g2d.draw(new java.awt.geom.Line2D.Double(sx, sy, ex, ey));
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

    @Override
    public String toString() {
        return "Wire(a:"+ElectricalComponent.allComponents.indexOf(startComponent)+"|b:"+
                ElectricalComponent.allComponents.indexOf(endComponent)+"|sI:"+startIndex+
                "|eI:"+endIndex+")";
    }

    public ElectricalComponent getStartComponent() {
        return startComponent;
    }

    public ElectricalComponent getEndComponent() {
        return endComponent;
    }
}
