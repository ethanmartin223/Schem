package Ocilliscope;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class OscilloscopeKnob extends JComponent {
    protected double eventvalue;

    private double min, max;
    private boolean dragging = false;
    private double lastAngle;
    private Image knobImage;

    private double turns;

    OscilloscopeDisplay display;

    public OscilloscopeKnob(OscilloscopeDisplay oc, Image knobImage, double min, double max, double value, double turns) {
        this.min = min;
        this.max = max;
        this.turns = turns;
        this.eventvalue = value;
        this.knobImage = knobImage;
        this.display = oc;

        setPreferredSize(new Dimension(60, 60));

        addMouseWheelListener(e -> {
            double step = (max - min) / (turns * 12.0);
            setValue(-e.getPreciseWheelRotation() * step);
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragging = true;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                lastAngle = angleToPoint(cx, cy, e.getX(), e.getY());
            }

            @Override public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    double angle = angleToPoint(cx, cy, e.getX(), e.getY());
                    double delta = angle - lastAngle;
                    if (delta > 180) delta -= 360;
                    if (delta < -180) delta += 360;
                    double valueDelta = (delta / 360.0) * (max - min) / turns;
                    setValue(valueDelta);

                    lastAngle = angle;
                }
            }
        });
    }

    public static double angleToPoint(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        return angle;
    }

    public void setValue(double v) {
        double old = eventvalue;
        this.eventvalue = Math.max(min, Math.min(max, eventvalue + v));
        firePropertyChange("value", old, eventvalue);
        repaint();
    }

    public double getValue() {
        return eventvalue;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(w, h);
        Image scaled = knobImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);

        double progress = (eventvalue - min) / (max - min);
        double totalAngle = progress * (turns * 360.0);
        double angleDeg = totalAngle % 360.0;
        double angleRad = Math.toRadians(angleDeg);

        int cx = w / 2;
        int cy = h / 2;

        AffineTransform old = g2.getTransform();
        g2.rotate(angleRad, cx, cy);
        g2.drawImage(scaled, cx - size / 2, cy - size / 2, null);
        g2.setTransform(old);

        g2.dispose();
    }
}
