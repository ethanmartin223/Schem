package Editor;

import ElectricalComponents.*;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;


public class ComponentRenderer {
    static HashMap<String, BufferedImage> buffer;
    public static final Color HIGHLIGHT_COLOR = new Color(255,153,0);

    //this method is expensive AF because it forces regenerating all assets,
    // try to avoid calling at all costs
    public static void clearBuffer() {
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

    public static BufferedImage render(DraggableEditorComponent caller, Graphics2D g, int cx, int cy, int size, String id, boolean isHighlight, double sizeOverride) {
        String key = id + "_" + size+"_"+isHighlight+"_"+
                ((caller==null)?null:
                (caller.getElectricalComponent().isIndividuallyRendered()?
                        caller.getElectricalComponent().hashCode()+"":null));
        if (buffer.containsKey(key)) return buffer.get(key);
        BufferedImage img = new BufferedImage((int) (size * sizeOverride), (int) (size * sizeOverride), BufferedImage.TYPE_INT_ARGB);
        return createImageFromBuffer(caller,cx, cy, size, id, isHighlight, key, img);
    }

    private static BufferedImage createImageFromBuffer(DraggableEditorComponent caller, int cx, int cy, int size, String id, boolean isHighlight, String key, BufferedImage img) {
        Graphics2D g2d = img.createGraphics();
        setHints(g2d);

        if (isHighlight) {
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.setStroke(new BasicStroke((float) Math.max(.06 * size, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            renderDirect(caller, g2d, cx, cy, size, id);
        }

        g2d.setStroke(new BasicStroke(Math.max(1f, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.BLACK);
        renderDirect(caller, g2d, cx, cy, size, id);

        g2d.dispose();
        buffer.put(key, img);
        return img;
    }


    public static void renderDirect(DraggableEditorComponent caller, Graphics2D g2d, int cx, int cy, int size, String id) {
        switch (id) {
            case ANDGate.id -> drawAND(g2d, cx, cy, size);
            case Capacitor.id -> drawCapacitor(g2d, cx, cy, size);
            case Diode.id -> drawDiode(g2d, cx, cy, size);
            case Ground.id -> drawGround(g2d, cx, cy, size);
            case NANDGate.id -> drawNAND(g2d, cx, cy, size);
            case NpnTransistor.id -> drawNPNTransistor(g2d, cx, cy, size);
            case ORGate.id -> drawOR(g2d, cx, cy, size);
            case PnpTransistor.id -> drawPNPTransistor(g2d, cx, cy, size, id);
            case PowerSupply.id -> drawPowerSupply(g2d, cx, cy, size);
            case Resistor.id -> drawResistor(g2d, cx, cy, size);
            case Transformer.id -> drawTransformer(g2d, cx, cy, size);
            case VariableResistor.id -> drawVariableResistor(g2d, cx, cy, size);
            case XORGate.id -> drawXOR(g2d, cx, cy, size);
            case ZenerDiode.id -> drawZenerDiode(g2d, cx, cy, size);
            case Speaker.id -> drawSpeaker(g2d, cx, cy, size);
            case Lamp.id -> drawLamp(g2d, cx, cy, size);
            case WireNode.id -> drawWireNode(g2d, cx, cy, size);
            case Microphone.id -> drawMicrophone(g2d, cx, cy, size);
            case ElectricalComponents.LED.id -> drawLED(g2d, cx, cy, size);
            case Photoresistor.id -> drawPhotoresistor(g2d, cx, cy, size);
            case IntegratedCircuit.id -> drawIC(g2d, cx, cy, size,
                    caller != null ? (int) caller.getElectricalComponent().electricalProperties.get("number_of_pins") : 16,
                    true);
            default -> {
                System.err.println("Unknown component ID: " + id);
            }
        }
    }


    private static void drawIC(Graphics2D g2d, int cx, int cy, int size, int leadCount, boolean showPinNumbers) {
        g2d.setStroke(new BasicStroke(Math.max(1f, size * EditorArea.DEBUG_NATIVE_DRAW_SIZE),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Calculate IC body dimensions
        double bodyWidth = size;
        double bodyHeight = (leadCount / 2) * (size * 0.2);
        bodyHeight = Math.max(bodyHeight, size * 0.8); // Minimum height

        // Calculate lead parameters
        double leadLength = size * 0.2;
        double leadSpacing = bodyHeight / (leadCount / 2);

        // Draw IC body
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

    private static BufferedImage drawSpeaker(Graphics2D g2d, int cx, int cy, int size) {
        // Proportions (similar style to your drawAND)
        double totalWidth  = size * 0.5;   // whole speaker area (housing + cone)
        double totalHeight = size * 0.4;   // overall height
        double housingFrac = 0.250;         // fraction of totalWidth used by housing
        double driverFrac  = 6;         // fraction of totalHeight used by driver circle
        double waveGapFrac = 0.1;         // spacing between sound-wave arcs

        // Compute integer coordinates
        int width  = (int) Math.round(totalWidth);
        int height = (int) Math.round(totalHeight);
        int halfH  = height / 2;

        // Housing (rounded rectangle on the left)
        int housingW = (int) Math.round(width * housingFrac);
        int housingX = cx - width / 2;
        int housingY = cy - halfH;
        int arcRadius = Math.max(1, (int)(size * 0.06)); // corner roundness
        g2d.drawRect(housingX, housingY,
                housingW, height);

        int driverR = (int) (Math.round(height * driverFrac) / 4);

        // Cone / horn area (a trapezoid-like polygon from housing edge -> right)
        int coneLeftX = housingX + housingW;
        int coneRightX = housingX + width;
        int coneTopY = housingY + (int)Math.round(height * -.5);
        int coneBottomY = housingY + (int)Math.round(height *1.5);

        Polygon cone = new Polygon();
        // Start near the driver vertical span for a smooth transition
        cone.addPoint(coneLeftX, cy - (int)Math.round(driverR * 0.35));
        cone.addPoint(coneRightX, coneTopY);
        cone.addPoint(coneRightX, coneBottomY);
        cone.addPoint(coneLeftX, cy + (int)Math.round(driverR * 0.35));
        g2d.drawPolygon(cone);

        // Sound-wave arcs to the right of the cone (3 arcs spaced evenly)
        int waves = 3;
        for (int i = 0; i < waves; i++) {
            int r = (int)Math.round(size * 0.1) + i * (int)Math.round(size * waveGapFrac);
            // center the arc roughly at the cone tip area
            int arcX = coneRightX - r / 2 + (int)Math.round(size * 0.05);
            int arcY = cy - r / 2;
            // Draw a wide arc (~90 degrees) to mimic sound waves
            g2d.drawArc(arcX, arcY, r, r, -45, 90);
        }

        // Match your drawAND signature (you returned null there), so return null here as well
        return null;
    }

    private static BufferedImage drawMicrophone(Graphics2D g2d, int cx, int cy, int size) {
        // Dimensions
        int headDiam   = (int) Math.round(size * 0.50); // circle diameter
        int headR      = headDiam / 2;
        int tangentLen = Math.max(6, (int) Math.round(size * 0.50)); // vertical tangent length
        int leadSpacing = (int) Math.round(headDiam * 0.31); // spacing between leads (on 0.1 multiples)
        int leadLen    = (int) Math.round(size * 0.25); // horizontal lead length

        // Draw circle
        g2d.drawOval(cx - headR, cy - headR, headDiam, headDiam);

        // Left-side vertical tangent line (centered at circle midpoint)
        int tangentX = cx - headR;
        int tHalf = tangentLen / 2;
        g2d.drawLine(tangentX, cy - tHalf, tangentX, cy + tHalf);

        // Calculate exact connection points on the circle for each lead
        // y-offsets from center
        int offsetTop = (int)(-leadSpacing *1.3);
        int offsetBot = (int) (leadSpacing *1.3);

        // Compute intersection x for each offset: x = cx + sqrt(r^2 - dy^2)
        double dyTop = offsetTop;
        double dyBot = offsetBot;
        int startXTop = (int) Math.round(cx + Math.sqrt(headR * headR - dyTop * dyTop));
        int startXBot = (int) Math.round(cx + Math.sqrt(headR * headR - dyBot * dyBot));

        int yTop = cy + offsetTop;
        int yBot = cy + offsetBot;

        // Draw leads starting at circle perimeter
        g2d.drawLine(startXTop, yTop, startXTop + leadLen, yTop);
        g2d.drawLine(startXBot, yBot, startXBot + leadLen, yBot);

        return null;
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

    private static BufferedImage drawLED(Graphics2D g2d, int cx, int cy, int size) {
        int pinLength  = (int) (size * 0.2);
        int bodyLength = (int) (size * 0.4);
        int halfHeight = (int) (size * 0.2);

        // diode leads
        g2d.drawLine(cx - pinLength - bodyLength / 2, cy, cx - bodyLength / 2, cy);
        g2d.drawLine(cx + bodyLength / 2, cy, cx + pinLength + bodyLength / 2, cy);

        // diode triangle
        Polygon triangle = new Polygon();
        triangle.addPoint(cx - bodyLength / 2, cy - halfHeight);
        triangle.addPoint(cx - bodyLength / 2, cy + halfHeight);
        triangle.addPoint(cx + bodyLength / 2, cy);
        g2d.drawPolygon(triangle);

        // diode bar
        g2d.drawLine(cx + bodyLength / 2, cy - halfHeight, cx + bodyLength / 2, cy + halfHeight);

        // --- LED emission arrows ---
        double arrowLen   = Math.max(3, size * 0.20);  // shaft length
        double headLen    = Math.max(2, size * 0.06);  // arrowhead size along shaft
        double headWidth  = Math.max(2, size * 0.05);  // arrowhead half-width

        int shiftLeft = (int) (bodyLength * 0.2);

        int startY = cy - halfHeight - (int) (size * 0.04);
        int tipY   = startY - (int) Math.round(arrowLen * Math.sqrt(2) / 2);

        int dy = startY - tipY;
        int dx = dy; // keep 45° direction

        // First arrow
        int ax1 = cx + bodyLength / 2 - shiftLeft;
        int ay1 = startY;
        int tip1x = ax1 + dx;
        int tip1y = tipY;

        // Second arrow
        int ax2 = ax1 + (int) (size * 0.12);
        int ay2 = startY;
        int tip2x = ax2 + dx;
        int tip2y = tipY;

        // Draw shafts
        g2d.drawLine(ax1, ay1, tip1x, tip1y);
        g2d.drawLine(ax2, ay2, tip2x, tip2y);

        // Helper: draw open arrowhead (two lines)
        java.util.function.BiConsumer<int[], int[]> drawOpenHead = (base, tip) -> {
            double vx = tip[0] - base[0];
            double vy = tip[1] - base[1];
            double vlen = Math.hypot(vx, vy);
            if (vlen == 0) vlen = 1;
            double ux = vx / vlen, uy = vy / vlen;
            double px = -uy, py = ux;

            int hx1 = (int) Math.round(tip[0] - ux * headLen + px * headWidth);
            int hy1 = (int) Math.round(tip[1] - uy * headLen + py * headWidth);
            int hx2 = (int) Math.round(tip[0] - ux * headLen - px * headWidth);
            int hy2 = (int) Math.round(tip[1] - uy * headLen - py * headWidth);

            g2d.drawLine(tip[0], tip[1], hx1, hy1);
            g2d.drawLine(tip[0], tip[1], hx2, hy2);
        };

        // Draw arrowheads (open)
        drawOpenHead.accept(new int[]{ax1, ay1}, new int[]{tip1x, tip1y});
        drawOpenHead.accept(new int[]{ax2, ay2}, new int[]{tip2x, tip2y});

        return null;
    }

    private static BufferedImage drawPhotoresistor(Graphics2D g2d, int cx, int cy, int size) {
        int pinLength  = (int) (size * 0.25);
        int bodyLength = (int) (size * 0.5);
        int halfHeight = (int) (size * 0.2);

        // resistor body (rectangle)
        int bodyLeft  = cx - bodyLength / 2;
        int bodyRight = cx + bodyLength / 2;
        int bodyTop   = cy - halfHeight;
        int bodyBot   = cy + halfHeight;

        g2d.drawRect(bodyLeft, bodyTop, bodyLength, halfHeight * 2);

        // leads
        g2d.drawLine(bodyLeft - pinLength, cy, bodyLeft, cy);
        g2d.drawLine(bodyRight, cy, bodyRight + pinLength, cy);

        // --- Light arrows (pointing toward the resistor) ---
        double arrowLen   = Math.max(3, size * 0.20);  // shaft length
        double headLen    = Math.max(2, size * 0.06);
        double headWidth  = Math.max(2, size * 0.05);

        int shiftLeft = (int) (bodyLength * 0.2);

        // Parallel arrow placement (now "tip" is closer to resistor body)
        int tipY = cy - halfHeight - (int) (size * 0.04); // tip near top of resistor
        int startY = tipY - (int) Math.round(arrowLen * Math.sqrt(2) / 2);

        int dy = tipY - startY;
        int dx = dy; // still 45°

        // First arrow (closer to left)
        int ax1 = bodyLeft + shiftLeft;
        int ay1 = startY;
        int tip1x = ax1 + dx;
        int tip1y = tipY;

        // Second arrow (to the right, same y-levels)
        int ax2 = ax1 + (int) (size * 0.12);
        int ay2 = startY;
        int tip2x = ax2 + dx;
        int tip2y = tipY;

        // Shafts (pointing toward resistor)
        g2d.drawLine(ax1, ay1, tip1x, tip1y);
        g2d.drawLine(ax2, ay2, tip2x, tip2y);

        // Open arrowhead pointing TOWARD tip (i.e., toward resistor)
        java.util.function.BiConsumer<int[], int[]> drawOpenHead = (base, tip) -> {
            double vx = base[0] - tip[0];  // reversed direction
            double vy = base[1] - tip[1];
            double vlen = Math.hypot(vx, vy);
            if (vlen == 0) vlen = 1;
            double ux = vx / vlen, uy = vy / vlen;
            double px = -uy, py = ux;

            int hx1 = (int) Math.round(tip[0] + ux * headLen + px * headWidth);
            int hy1 = (int) Math.round(tip[1] + uy * headLen + py * headWidth);
            int hx2 = (int) Math.round(tip[0] + ux * headLen - px * headWidth);
            int hy2 = (int) Math.round(tip[1] + uy * headLen - py * headWidth);

            g2d.drawLine(tip[0], tip[1], hx1, hy1);
            g2d.drawLine(tip[0], tip[1], hx2, hy2);
        };

        // Draw arrowheads (now pointing inward)
        drawOpenHead.accept(new int[]{ax1, ay1}, new int[]{tip1x, tip1y});
        drawOpenHead.accept(new int[]{ax2, ay2}, new int[]{tip2x, tip2y});

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
