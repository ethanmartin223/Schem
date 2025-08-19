package Ocilliscope;

import java.awt.*;
import java.util.Arrays;

public class OscilloscopeTrace {

    public boolean isVisible;
    Color color;
    private OscilloscopeDisplay oscilloscopeDisplay;
    private Image traceIdentifierImage;

    double voltageScale;
    double vertShift;
    double horizShift;
    private double[] function;

    public OscilloscopeTrace(OscilloscopeDisplay oc, Color traceColor, Image identiferImage) {
        color = traceColor;
        oscilloscopeDisplay = oc;
        traceIdentifierImage = identiferImage;
        isVisible = true;
    }

    private static double getYSinValue(double x, double amplitude, double period,
                                       double phaseShift, double verticalShift) {
        return amplitude * Math.sin(period * (x - phaseShift)) + verticalShift;
    }

    public void renderTrace(Graphics2D g2d) {
        if (isVisible) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(3f));

            int width = oscilloscopeDisplay.getWidth() - oscilloscopeDisplay.leftMargin;
            int[] yPoints = new int[width];
            int[] xPoints = new int[width];
            for (int x = 0; x < width; x++) {
                yPoints[x] = (int) (vertShift + function[x]);
                xPoints[x] = x + oscilloscopeDisplay.leftMargin;
            }

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            Image scaled = traceIdentifierImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            g2d.drawImage(scaled, 0, (int) ((vertShift + (double) oscilloscopeDisplay.getHeight() / 2) - 15), null);

            g2d.drawPolyline(xPoints, yPoints, width);
        }
    }

    public void setFunction(String waveform, double freq, double amp, double offset, double sampleRate, double duration) {
        int samples = (int) (sampleRate * duration);
        offset+= oscilloscopeDisplay.getHeight()/2.0;
        double[] y = new double[samples];
        for (int i = 0; i < samples; i++) {
            double t = i / sampleRate; // current time in seconds
            double value = switch (waveform.toLowerCase()) {
                case "sine" -> amp * Math.sin(2 * Math.PI * freq * t) + offset;
                case "square" -> amp * (Math.sin(2 * Math.PI * freq * t) >= 0 ? 1 : -1) + offset;
                case "triangle" -> (2 * amp / Math.PI) * Math.asin(Math.sin(2 * Math.PI * freq * t)) + offset;
                default -> throw new IllegalArgumentException("Unknown waveform: " + waveform);
            };
            y[i] = value;
        }

        function = y;
    }
}
