package Editor;

import ElectricalComponents.IntegratedCircuit;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;

import static ElectronicsBackend.ElectricalComponentIdentifier.*;

public class ComponentRenderer {
    static HashMap<String, BufferedImage> buffer;
    public static final Color HIGHLIGHT_COLOR = new Color(255,153,0);

    //this method is expensive AF because it forces regenerating all assets,
    // try to avoid calling at all costs
    static void clearBuffer() {
        buffer.clear();
    }

    static {
        buffer = new HashMap<>();

    }

    static void setHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    static boolean existsInBuffer(String id) {
        return buffer.containsKey(id);
    }


    public static void renderDirect(Graphics2D g2d, int cx, int cy, int size, String id) {
        if (id.equals(AND_GATE.id)) drawAND(g2d, cx, cy, size);
        else if (id.equals(CAPACITOR.id)) drawCapacitor(g2d, cx, cy, size);
        else if (id.equals(DIODE.id)) drawDiode(g2d, cx, cy, size);
        else if (id.equals(GROUND.id)) drawGround(g2d, cx, cy, size);
        else if (id.equals(NAND_GATE.id)) drawNAND(g2d, cx, cy, size);
        else if (id.equals(NPN_TRANSISTOR.id)) drawNPNTransistor(g2d, cx, cy, size);
        else if (id.equals(OR_GATE.id)) drawOR(g2d, cx, cy, size);
        else if (id.equals(PNP_TRANSISTOR.id)) drawPNPTransistor(g2d, cx, cy, size, id);
        else if (id.equals(POWERSUPPLY.id)) drawPowerSupply(g2d, cx, cy, size);
        else if (id.equals(RESISTOR.id)) drawResistor(g2d, cx, cy, size);
        else if (id.equals(TRANSFORMER.id)) drawTransformer(g2d, cx, cy, size);
        else if (id.equals(VARIABLE_RESISTOR.id)) drawVariableResistor(g2d, cx, cy, size);
        else if (id.equals(XOR_GATE.id)) drawXOR(g2d, cx, cy, size);
        else if (id.equals(ZENER_DIODE.id)) drawZenerDiode(g2d, cx, cy, size);
        else if (id.equals(SPEAKER.id)) drawSpeaker(g2d, cx, cy, size);
        else if (id.equals(LAMP.id)) drawLamp(g2d, cx, cy, size);
        else if (id.equals(WIRE_NODE.id)) drawWireNode(g2d, cx, cy, size);
        else if (id.equals(INTEGRATED_CIRCUIT.id)) drawIC(g2d, cx, cy, size, 8, true);
    }

    private static void drawIC(Graphics2D g2d, int cx, int cy, int size, int leadCount, boolean showPinNumbers) {
        // Calculate IC body dimensions
        double bodyWidth = size;
        double bodyHeight = (leadCount / 2) * (size * 0.2);
        bodyHeight = Math.max(bodyHeight, size * 0.8); // Minimum height

        // Calculate lead parameters
        double leadLength = size * 0.2;
        double leadSpacing = bodyHeight / (leadCount / 2);

        // Draw IC body
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        Rectangle2D body = new Rectangle2D.Double(
                cx - bodyWidth / 2,
                cy - bodyHeight / 2,
                bodyWidth,
                bodyHeight
        );
        g2d.draw(body);

        // Draw notch at top to indicate orientation
        int notchSize = size / 10;
        g2d.drawOval((int) (cx + bodyWidth / 2 - notchSize * 2), (int) (cy - bodyHeight / 2 + notchSize * 1.5),
                notchSize, notchSize);

        // Draw left leads
        for (int i = 0; i < leadCount / 2; i++) {
            double y = cy - bodyHeight / 2 + (i + 0.5) * leadSpacing;
            g2d.drawLine((int) (cx - bodyWidth / 2), (int) y,
                    (int) (cx - bodyWidth / 2 - leadLength), (int) y);

            if (showPinNumbers) {
                drawPinNumber(g2d, (int) (cx - bodyWidth / 2 - leadLength - 5), (int) y, i + 1);
            }
        }

        // Draw right leads
        for (int i = 0; i < leadCount / 2; i++) {
            double y = cy + bodyHeight / 2 - (i + 0.5) * leadSpacing;
            g2d.drawLine((int) (cx + bodyWidth / 2), (int) y,
                    (int) (cx + bodyWidth / 2 + leadLength), (int) y);

            if (showPinNumbers) {
                drawPinNumber(g2d, (int) (cx + bodyWidth / 2 + leadLength + 5), (int) y, leadCount - i);
            }
        }
    }

    private static void drawPinNumber(Graphics2D g2d, int x, int y, int number) {
        String text = Integer.toString(number);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        g2d.drawString(text, x - textWidth / 2, y + textHeight / 4);
    }

    private static void drawSpeaker(Graphics2D g2d, int cx, int cy, int size) {

    }

    private static void drawWireNode(Graphics2D g2d, int cx, int cy, int size) {
        g2d.setStroke(new BasicStroke(Math.max(1f, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.BLACK);

        // Radius of the dot
        int radius = (int) (size * 0.025);

        // Draw centered filled circle
        g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    public static BufferedImage render(Graphics2D g, int cx, int cy, int size, String id, boolean isHighlight) {
        String key = id + "_" + size+"_"+isHighlight;
        if (buffer.containsKey(key)) return buffer.get(key);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        setHints(g2d);

        if (isHighlight) {
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.setStroke(new BasicStroke((float) Math.max(.06 * size, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            renderDirect(g2d, cx, cy, size, id);
        }

        g2d.setStroke(new BasicStroke(Math.max(1f, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.BLACK);
        renderDirect(g2d, cx, cy, size, id);

        g2d.dispose();
        buffer.put(key, img);
        return img;
    }

    private static BufferedImage drawAND(Graphics2D g2d, int cx, int cy, int size) {
        // Proportions
        double width = size * 0.5;       // total width of AND gate
        double height = size * 0.5;      // total height
        double radius = height / 2;      // for curved side

        // Configurable input spacing factor (fraction of height)
        double inputSpacingFactor = 0.8;
        double inputSpacing = height * inputSpacingFactor;

        // Left vertical line (input side)
        int leftX = (int)(cx - width / 2);
        int topY = (int)(cy - height / 2);
        int bottomY = (int)(cy + height / 2);
        g2d.drawLine(leftX, topY, leftX, bottomY);

        // Top and bottom horizontal lines (connect left and semicircle)
        int arcX = leftX;
        int arcWidth = (int)width;
        int arcHeight = (int)height;
        g2d.drawLine(leftX, topY, leftX + (int)(arcWidth / 2), topY);       // top horizontal
        g2d.drawLine(leftX, bottomY, leftX + (int)(arcWidth / 2), bottomY); // bottom horizontal

        // Symmetrically spaced input lines (shorter now)
        int centerY = (topY + bottomY) / 2;
        int input1Y = (int)(centerY - inputSpacing / 2);
        int input2Y = (int)(centerY + inputSpacing / 2);
        int inputLength = (int)(size * 0.15); // shorter input leads
        g2d.drawLine(leftX - inputLength, input1Y, leftX, input1Y);
        g2d.drawLine(leftX - inputLength, input2Y, leftX, input2Y);

        // Right side: curved semicircle
        int arcY = topY;
        g2d.drawArc(arcX, arcY, arcWidth, arcHeight, -90, 180);

        // Output line (right)
        int rightX = leftX + (int)width;
        g2d.drawLine(rightX, centerY, rightX + (int)(size * 0.15), centerY);
        return null;
    }

    private static BufferedImage drawCapacitor(Graphics2D g2d, int cx, int cy, int size) {
        int half = size / 10;
        g2d.drawLine(cx - half, cy - size / 4, cx - half, cy + size / 4);
        g2d.drawLine(cx + half, cy - size / 4, cx + half, cy + size / 4);

        g2d.drawLine((int) (cx - size / 2.5), cy, cx - half, cy);
        g2d.drawLine(cx + half, cy, (int) (cx + size / 2.5), cy);
        return null;
    }

    private static void drawLamp(Graphics2D g2d, int cx, int cy, int size) {


        // Total lamp width
        double width = size * 0.8;
        double height = size * 0.5;

        // Left and right lead positions
        int leftX = (int) (cx - width / 2);
        int rightX = (int) (cx + width / 2);
        int centerY = cy;

        // Circle radius (slightly smaller than half height)
        int circleRadius = (int) (height * 0.4);

        // Draw leads
        g2d.drawLine(leftX, centerY, cx - circleRadius, centerY);  // left lead
        g2d.drawLine(cx + circleRadius, centerY, rightX, centerY); // right lead

        // Draw circle in the center
        int circleX = cx - circleRadius;
        int circleY = cy - circleRadius;
        g2d.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);

        // Calculate the actual radius to keep X within the circle
        double effectiveRadius = circleRadius;

        // Draw X inside the circle using trigonometric positioning
        double angle = Math.PI / 4; // 45 degrees in radians
        int x1 = cx + (int) (effectiveRadius * Math.cos(angle));
        int y1 = cy + (int) (effectiveRadius * Math.sin(angle));
        int x2 = cx - (int) (effectiveRadius * Math.cos(angle));
        int y2 = cy - (int) (effectiveRadius * Math.sin(angle));
        int x3 = cx + (int) (effectiveRadius * Math.cos(angle));
        int y3 = cy - (int) (effectiveRadius * Math.sin(angle));
        int x4 = cx - (int) (effectiveRadius * Math.cos(angle));
        int y4 = cy + (int) (effectiveRadius * Math.sin(angle));

        g2d.drawLine(x1, y1, x2, y2);
        g2d.drawLine(x3, y3, x4, y4);
    }

    private static BufferedImage drawDiode(Graphics2D g2d, int cx, int cy, int size) {
        int pinLength = (int) (size * 0.2);
        int bodyLength = (int) (size * 0.4);
        int halfHeight = (int) (size * 0.2);
        g2d.drawLine(cx - pinLength - bodyLength / 2, cy, cx - bodyLength / 2, cy);
        g2d.drawLine(cx + bodyLength / 2, cy, cx + pinLength + bodyLength / 2, cy);
        Polygon triangle = new Polygon();
        triangle.addPoint(cx - bodyLength / 2, cy - halfHeight);
        triangle.addPoint(cx - bodyLength / 2, cy + halfHeight);
        triangle.addPoint(cx + bodyLength / 2, cy);
        g2d.drawPolygon(triangle);
        g2d.drawLine(cx + bodyLength / 2, cy - halfHeight, cx + bodyLength / 2, cy + halfHeight);
        return null;
    }

    private static BufferedImage drawGround(Graphics2D g2d, int cx, int cy, int size) {
        int step = size / 8;
        int width = size / 5;
        g2d.drawLine(cx, cy, cx, cy + step);
        g2d.drawLine(cx - width, cy + step, cx + width, cy + step);
        g2d.drawLine(cx - width / 2, cy + 2 * step, cx + width / 2, cy + 2 * step);
        g2d.drawLine(cx - width / 4, cy + 3 * step, cx + width / 4, cy + 3 * step);
        return null;
    }

    private static BufferedImage drawNAND(Graphics2D g2d, int cx, int cy, int size) {
        // Proportions
        double width = size * 0.5;       // total width of gate
        double height = size * 0.5;      // total height
        double radius = height / 2;      // for curved side
        double negationRadius = size * 0.05; // radius of negation circle at output

        // Configurable input spacing factor (fraction of height)
        double inputSpacingFactor = 0.8;
        double inputSpacing = height * inputSpacingFactor;

        // Left vertical line (input side)
        int leftX = (int)(cx - width / 2);
        int topY = (int)(cy - height / 2);
        int bottomY = (int)(cy + height / 2);
        g2d.drawLine(leftX, topY, leftX, bottomY);

        // Top and bottom horizontal lines (connect left and semicircle)
        int arcX = leftX;
        int arcWidth = (int)width;
        int arcHeight = (int)height;
        g2d.drawLine(leftX, topY, leftX + (int)(arcWidth / 2), topY);       // top horizontal
        g2d.drawLine(leftX, bottomY, leftX + (int)(arcWidth / 2), bottomY); // bottom horizontal

        // Symmetrically spaced input lines
        int centerY = (topY + bottomY) / 2;
        int input1Y = (int)(centerY - inputSpacing / 2);
        int input2Y = (int)(centerY + inputSpacing / 2);
        int inputLength = (int)(size * 0.15); // input leads
        g2d.drawLine(leftX - inputLength, input1Y, leftX, input1Y);
        g2d.drawLine(leftX - inputLength, input2Y, leftX, input2Y);

        // Right side: curved semicircle
        int arcY = topY;
        g2d.drawArc(arcX, arcY, arcWidth, arcHeight, -90, 180);

        // Output line (right)
        int negRad = (int)negationRadius;
        int rightX = leftX + arcWidth;
        int outputLength = inputLength + (int)(negationRadius * .25); // leave space for negation circle
        g2d.drawLine(rightX+negRad*2, centerY, rightX + outputLength, centerY);

        // Draw negation circle at output
        int circleX = rightX + negRad;
        int circleY = centerY;
        g2d.drawOval(circleX - negRad, circleY - negRad, 2 * negRad, 2 * negRad);
        return null;
    }

    private static BufferedImage drawNPNTransistor(Graphics2D g2d, int cx, int cy, int size) {
        double r = size * 0.3;        // circle radius
        double leadLength = size * 0.4;
        double arrowLength = size * 0.08;
        double baseLineLength = size * 0.25;
        double baseShiftLeft = size * 0.09; // shift base junction left

        int baseJunctionX = cx - (int)baseShiftLeft;
        int baseJunctionY = cy;

        g2d.drawOval((int)(cx - r), (int)(cy - r), (int)(2*r), (int)(2*r));

        int baseCenterY1 = baseJunctionY - (int)(baseLineLength/2);
        int baseCenterY2 = baseJunctionY + (int)(baseLineLength/2);
        g2d.drawLine(baseJunctionX, baseCenterY1, baseJunctionX, baseCenterY2);

        int baseX1 = (int)(cx - leadLength);
        g2d.drawLine(baseX1, baseJunctionY, baseJunctionX, baseJunctionY);

        int collectorBendX = (int)(cx + r * 0.34); // closer to circle
        int collectorBendY = (int)(cy - r * .55);
        int collectorEndX = collectorBendX;
        int collectorEndY = (int)(cy - r - leadLength*0.25);
        g2d.drawLine(baseJunctionX, baseJunctionY, collectorBendX, collectorBendY);
        g2d.drawLine(collectorBendX, collectorBendY, collectorEndX, collectorEndY);

        int emitterBendX = (int)(cx + r * 0.34);
        int emitterBendY = (int)(cy + r * 0.55);
        int emitterEndX = emitterBendX;
        int emitterEndY = (int)(cy + r + leadLength*0.25);
        g2d.drawLine(baseJunctionX, baseJunctionY, emitterBendX, emitterBendY);
        g2d.drawLine(emitterBendX, emitterBendY, emitterEndX, emitterEndY);

        double angle = Math.atan2(emitterBendY - baseJunctionY, emitterBendX - baseJunctionX);
        int arrowX1 = (int)(emitterBendX - arrowLength * Math.cos(angle - Math.PI/6));
        int arrowY1 = (int)(emitterBendY - arrowLength * Math.sin(angle - Math.PI/6));
        int arrowX2 = (int)(emitterBendX - arrowLength * Math.cos(angle + Math.PI/6));
        int arrowY2 = (int)(emitterBendY - arrowLength * Math.sin(angle + Math.PI/6));
        g2d.drawLine(emitterBendX, emitterBendY, arrowX1, arrowY1);
        g2d.drawLine(emitterBendX, emitterBendY, arrowX2, arrowY2);

        return null;
    }

    private static BufferedImage drawOR(Graphics2D g2d, int cx, int cy, int size) {
        // Proportions
        double width = size * 0.5;       // total width of OR gate
        double height = size * 0.5;      // total height

        // Configurable input spacing factor (fraction of height)
        double inputSpacingFactor = 0.8;
        double inputSpacing = height * inputSpacingFactor;

        int leftX = (int)(cx - width / 2);
        int topY = (int)(cy - height / 2);
        int bottomY = (int)(cy + height / 2);
        int centerY = (topY + bottomY) / 2;

        // Left convex arc (caves outward toward output)
        int leftArcWidth = (int)(width / 2);
        int leftArcX = leftX - leftArcWidth / 2;
        g2d.drawArc(leftArcX, topY, leftArcWidth, (int)height, 90, -180);

        // Top and bottom horizontal lines connecting left arc to semicircle
        int mainArcWidth = (int)width;
        int mainArcHeight = (int)height;
        g2d.drawLine(leftX, topY, leftX + mainArcWidth / 2, topY);       // top horizontal
        g2d.drawLine(leftX, bottomY, leftX + mainArcWidth / 2, bottomY); // bottom horizontal

        // Right convex semicircle
        int arcY = topY;
        g2d.drawArc(leftX, arcY, mainArcWidth, mainArcHeight, -90, 180);

        // Symmetrically spaced input leads extended to reach inner part of left arc
        int input1Y = (int)(centerY - inputSpacing / 2);
        int input2Y = (int)(centerY + inputSpacing / 2);
        int inputLength = (int)(size * 0.15); // extension beyond original starting point
        // Extend past the arc start by ~1/4 of leftArcWidth
        int inputEndX = (leftArcX + (int)(leftArcWidth/2*1.6));
        g2d.drawLine(leftX - inputLength, input1Y, inputEndX, input1Y);
        g2d.drawLine(leftX - inputLength, input2Y, inputEndX, input2Y);

        // Output lead
        int rightX = leftX + mainArcWidth;
        g2d.drawLine(rightX, centerY, rightX + (int)(inputLength), centerY);
        return null;
    }

    private static BufferedImage drawPNPTransistor(Graphics2D g2d, int cx, int cy, int size, String id) {
        // Proportions
        double r = size * 0.3;        // circle radius
        double leadLength = size * 0.4;
        double arrowLength = size * 0.08;
        double baseLineLength = size * 0.25;
        double baseShiftLeft = size * 0.09; // shift base junction left
        double arrowShiftFactor = 0.15;     // slightly more along emitter line

        // Shifted base junction
        int baseJunctionX = cx - (int)baseShiftLeft;
        int baseJunctionY = cy;

        // Draw transistor circle
        g2d.drawOval((int)(cx - r), (int)(cy - r), (int)(2*r), (int)(2*r));

        // Vertical center line at base
        int baseCenterY1 = baseJunctionY - (int)(baseLineLength/2);
        int baseCenterY2 = baseJunctionY + (int)(baseLineLength/2);
        g2d.drawLine(baseJunctionX, baseCenterY1, baseJunctionX, baseCenterY2);

        // Horizontal base lead (left toward junction)
        int baseX1 = (int)(cx - leadLength);
        g2d.drawLine(baseX1, baseJunctionY, baseJunctionX, baseJunctionY);

        // Collector lead: bend slightly inside circle
        int collectorBendX = (int)(cx + r * 0.34);
        int collectorBendY = (int)(cy - r * 0.55);
        int collectorEndX = collectorBendX;
        int collectorEndY = (int)(cy - r - leadLength*0.25);
        g2d.drawLine(baseJunctionX, baseJunctionY, collectorBendX, collectorBendY);
        g2d.drawLine(collectorBendX, collectorBendY, collectorEndX, collectorEndY);

        // Emitter lead: bend slightly inside circle
        int emitterBendX = (int)(cx + r * 0.34);
        int emitterBendY = (int)(cy + r * 0.55);
        int emitterEndX = emitterBendX;
        int emitterEndY = (int)(cy + r + leadLength*0.25);
        g2d.drawLine(baseJunctionX, baseJunctionY, emitterBendX, emitterBendY);
        g2d.drawLine(emitterBendX, emitterBendY, emitterEndX, emitterEndY);

        // Arrow on emitter (PNP: slightly more up along emitter line)
        double angle = Math.atan2(emitterBendY - baseJunctionY, emitterBendX - baseJunctionX);
        int arrowBaseX = (int)(baseJunctionX + arrowShiftFactor * (emitterBendX - baseJunctionX));
        int arrowBaseY = (int)(baseJunctionY + arrowShiftFactor * (emitterBendY - baseJunctionY));
        int arrowX1 = (int)(arrowBaseX + arrowLength * Math.cos(angle - Math.PI/6));
        int arrowY1 = (int)(arrowBaseY + arrowLength * Math.sin(angle - Math.PI/6));
        int arrowX2 = (int)(arrowBaseX + arrowLength * Math.cos(angle + Math.PI/6));
        int arrowY2 = (int)(arrowBaseY + arrowLength * Math.sin(angle + Math.PI/6));
        g2d.drawLine(arrowBaseX, arrowBaseY, arrowX1, arrowY1);
        g2d.drawLine(arrowBaseX, arrowBaseY, arrowX2, arrowY2);
        return null;
    }

    private static BufferedImage drawPowerSupply(Graphics2D g2d, int cx, int cy, int size) {
        int verticalLength = (int)(size * 0.3);   // length of vertical part of T
        int horizontalLength = (int)(size * 0.2); // half-length of horizontal part of T

        // Draw the horizontal top line of the T
        g2d.drawLine(cx - horizontalLength, cy, cx + horizontalLength, cy);

        // Draw the vertical line down from center
        g2d.drawLine(cx, cy, cx, cy + verticalLength);

        // Draw the label "5V" centered above the horizontal line
        g2d.setFont(new Font("Arial", Font.PLAIN, (int)(size * 0.15)));
        FontMetrics fm = g2d.getFontMetrics();
        String label = "5V";
        int textWidth = fm.stringWidth(label);
        g2d.drawString(label, cx - textWidth / 2, cy - (int)(size * 0.05));
        return null;
    }

    private static BufferedImage drawResistor(Graphics2D g2d, int cx, int cy, int size) {
        // Resistor dimensions
        double bodyWidth = size * 0.6;    // width of zig-zag body
        double amplitude = size * 0.2;    // vertical height of zig-zag (peak to center)
        int segments = 6;                 // number of zig-zag segments
        int leadLength = (int)(size * 0.1);

        // Lead coordinates
        double leftX = cx - bodyWidth / 2;
        double rightX = cx + bodyWidth / 2;
        double centerY = cy;

        // Draw left lead
        g2d.drawLine((int)(leftX - leadLength), (int)centerY, (int)leftX, (int)centerY);
        // Draw right lead
        g2d.drawLine((int)rightX, (int)centerY, (int)(rightX + leadLength), (int)centerY);

        // Zig-zag body as a continuous path
        Path2D.Double path = new Path2D.Double();
        path.moveTo(leftX, centerY); // start at left

        double segmentWidth = bodyWidth / segments;
        boolean up = true; // first peak goes up

        for (int i = 0; i < segments; i++) {
            double x = leftX + (i + 0.5) * segmentWidth; // middle of segment
            double y = up ? centerY - amplitude / 2 : centerY + amplitude / 2;
            path.lineTo(x, y);

            x = leftX + (i + 1) * segmentWidth; // end of segment
            path.lineTo(x, centerY); // back to center line
            up = !up;
        }

        g2d.draw(path);
        return null;
    }

    private static BufferedImage drawTransformer(Graphics2D g2d, int cx, int cy, int size) {
        // Transformer dimensions
        double coilWidth = size * 0.15;   // horizontal width of each vertical loop
        double coilHeight = size * 0.5;   // vertical height of each coil
        double spacing = size * 0.1;      // gap between coils
        int turns = 3;                     // number of vertical loops per coil
        int leadLength = (int)(size * 0.15);

        double leftCoilX = cx - coilWidth - spacing / 2;
        double rightCoilX = cx + spacing / 2;
        double topY = cy - coilHeight / 2;
        double bottomY = cy + coilHeight / 2;

        double loopHeight = coilHeight / turns;

        // Draw primary coil (left) facing right (toward center)
        for (int i = 0; i < turns; i++) {
            double y = topY + i * loopHeight;
            Arc2D.Double arc = new Arc2D.Double(leftCoilX, y, coilWidth, loopHeight, 90, -180, Arc2D.OPEN);
            g2d.draw(arc);
        }

        // Draw secondary coil (right) facing left (toward center)
        for (int i = 0; i < turns; i++) {
            double y = topY + i * loopHeight;
            Arc2D.Double arc = new Arc2D.Double(rightCoilX, y, coilWidth, loopHeight, 90, 180, Arc2D.OPEN);
            g2d.draw(arc);
        }

        // Draw vertical core lines
        g2d.drawLine((int)(leftCoilX + coilWidth / 2), (int)topY, (int)(leftCoilX + coilWidth / 2), (int)bottomY);
        g2d.drawLine((int)(rightCoilX + coilWidth / 2), (int)topY, (int)(rightCoilX + coilWidth / 2), (int)bottomY);

        // Draw 4 leads (2 on each side, slightly offset from corners)
        int offsetY = (int)(size * 0.1);

        // Left side leads
        g2d.drawLine((int)(leftCoilX - leadLength), (int)(topY + offsetY), (int)leftCoilX, (int)(topY + offsetY));
        g2d.drawLine((int)(leftCoilX - leadLength), (int)(bottomY - offsetY), (int)leftCoilX, (int)(bottomY - offsetY));

        // Right side leads
        g2d.drawLine((int)(rightCoilX + coilWidth), (int)(topY + offsetY), (int)(rightCoilX + coilWidth + leadLength), (int)(topY + offsetY));
        g2d.drawLine((int)(rightCoilX + coilWidth), (int)(bottomY - offsetY), (int)(rightCoilX + coilWidth + leadLength), (int)(bottomY - offsetY));
        return null;
    }

    private static BufferedImage drawVariableResistor(Graphics2D g2d, int cx, int cy, int size) {
        // Resistor body dimensions
        double bodyWidth = size * 0.6;
        double amplitude = size * 0.2;
        int segments = 6;
        int leadLength = (int)(size * 0.1);

        // Lead coordinates
        double leftX = cx - bodyWidth / 2;
        double rightX = cx + bodyWidth / 2;
        double centerY = cy;

        // Draw leads
        g2d.drawLine((int)(leftX - leadLength), (int)centerY, (int)leftX, (int)centerY);
        g2d.drawLine((int)rightX, (int)centerY, (int)(rightX + leadLength), (int)centerY);

        // Draw zig-zag resistor body
        Path2D.Double path = new Path2D.Double();
        path.moveTo(leftX, centerY);
        double segmentWidth = bodyWidth / segments;
        boolean up = true;

        for (int i = 0; i < segments; i++) {
            double x = leftX + (i + 0.5) * segmentWidth;
            double y = up ? centerY - amplitude / 2 : centerY + amplitude / 2;
            path.lineTo(x, y);

            x = leftX + (i + 1) * segmentWidth;
            path.lineTo(x, centerY);
            up = !up;
        }
        g2d.draw(path);

        // Draw the variable resistor arrow, nearly vertical
        int arrowMargin = (int)(size * 0.05);
        int arrowX1 = (int)(cx - size * 0.08);                  // slightly left of center
        int arrowY1 = (int)(centerY + amplitude  + arrowMargin);  // bottom
        int arrowX2 = (int)(cx + size * 0.08);                  // slightly right of center
        int arrowY2 = (int)(centerY - amplitude - arrowMargin); // higher top

        // Main arrow line
        g2d.drawLine(arrowX1, arrowY1, arrowX2, arrowY2);

        // Draw arrowhead as two lines (V shape)
        int arrowHeadLength = (int)(size * 0.05);
        double angle = Math.atan2(arrowY2 - arrowY1, arrowX2 - arrowX1);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        // Left wing of arrowhead
        int lx = (int)(arrowX2 - arrowHeadLength * cos + arrowHeadLength * sin);
        int ly = (int)(arrowY2 - arrowHeadLength * sin - arrowHeadLength * cos);
        g2d.drawLine(arrowX2, arrowY2, lx, ly);

        // Right wing of arrowhead
        int rx = (int)(arrowX2 - arrowHeadLength * cos - arrowHeadLength * sin);
        int ry = (int)(arrowY2 - arrowHeadLength * sin + arrowHeadLength * cos);
        g2d.drawLine(arrowX2, arrowY2, rx, ry);
        return null;
    }

    private static BufferedImage drawXOR(Graphics2D g2d, int cx, int cy, int size) {
        // Proportions
        double width = size * 0.6;   // total width of XOR gate
        double height = size * 0.5;  // total height

        // Configurable input spacing factor
        double inputSpacingFactor = 0.8;
        double inputSpacing = height * inputSpacingFactor;

        // Curve offsets
        double curveOffset = size * 0.08;      // distance of inner curve from gate
        double curveShiftRight = size * 0.05;  // shift endpoints right
        double extraCurveOffset = size * 0.12; // offset for the leftmost curve

        int topY = (int)(cy - height / 2);
        int bottomY = (int)(cy + height / 2);
        int leftX = (int)(cx - width / 2);

        int arcWidth = (int)width;
        int arcHeight = (int)height;

        // Middle curve
        int innerCurveX = (int)(leftX - curveOffset + curveShiftRight);
        int innerCurveWidth = (int)(width / 2);
        g2d.drawArc(innerCurveX, topY, innerCurveWidth, arcHeight, -90, 180);

        // Leftmost curve
        int extraLeftCurveX = (int)(leftX - curveOffset - extraCurveOffset + curveShiftRight);
        g2d.drawArc(extraLeftCurveX, topY, innerCurveWidth, arcHeight, -90, 180);

        // Outer curve
        int outerCurveX = leftX;
        g2d.drawArc(outerCurveX, topY, arcWidth, arcHeight, -90, 180);

        // Horizontal connectors (between middle arc and leftmost arc)
        int midLeftX = innerCurveX;          // left edge of middle arc box
        int leftmostRightX = extraLeftCurveX + innerCurveWidth; // right edge of leftmost arc box
        g2d.drawLine(innerCurveX, topY, midLeftX, topY);
        g2d.drawLine(innerCurveX, bottomY, midLeftX, bottomY);

        // Inputs
        int centerY = (topY + bottomY) / 2;
        int input1Y = (int)(centerY - inputSpacing / 2);
        int input2Y = (int)(centerY + inputSpacing / 2);
        int inputLength = (int)(size * 0.1);
        g2d.drawLine(leftX - inputLength, input1Y, leftX, input1Y);
        g2d.drawLine(leftX - inputLength, input2Y, leftX, input2Y);

        // Output
        int rightX = leftX + arcWidth;
        g2d.drawLine(rightX, centerY, rightX + inputLength, centerY);
        return null;
    }

    private static BufferedImage drawZenerDiode(Graphics2D g2d, int cx, int cy, int size) {
        int pinLength = (int)(size * 0.2);
        int bodyLength = (int)(size * 0.4);
        int halfHeight = (int)(size * 0.2);
        int leadLength = (int)(size * 0.3) / 3;

        // Left input lead
        g2d.drawLine(cx - pinLength - bodyLength / 2, cy, cx - bodyLength / 2, cy);

        // Right output lead
        g2d.drawLine(cx + bodyLength / 2, cy, cx + pinLength + bodyLength / 2, cy);

        // Triangle (diode symbol)
        Polygon triangle = new Polygon();
        triangle.addPoint(cx - bodyLength / 2, cy - halfHeight);
        triangle.addPoint(cx - bodyLength / 2, cy + halfHeight);
        triangle.addPoint(cx + bodyLength / 2, cy);
        g2d.drawPolygon(triangle);

        // Vertical line at the tip of triangle
        g2d.drawLine(cx + bodyLength / 2, cy - halfHeight, cx + bodyLength / 2, cy + halfHeight);

        // Top Zener horizontal lead (goes left)
        g2d.drawLine(cx + bodyLength / 2, cy - halfHeight, cx + bodyLength / 2 - leadLength, cy - halfHeight);

        // Bottom Zener horizontal lead (goes right)
        g2d.drawLine(cx + bodyLength / 2, cy + halfHeight, cx + bodyLength / 2 + leadLength, cy + halfHeight);
        return null;
    }
}
