package Ocilliscope;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class OscilloscopeDisplay extends JPanel {

    public int currentTraceNumber;
    Color BACKGROUND_GRID_COLOR = new Color(59,61,58);

    Color TRACE_1_COLOR = new Color(255,153,0);
    Color TRACE_2_COLOR = new Color(0, 89, 255);
    Color TRACE_3_COLOR = new Color(115, 134, 80);
    Color TRACE_4_COLOR = new Color(222, 65, 53);

    int MIN_SCALE = 10;
    private double offsetX, offsetY;

    double scale;
    double timeScale;

    int leftMargin = 30;

    OscilloscopeTrace currentTrace;
    OscilloscopeTrace trace1, trace2, trace3, trace4;
    private Image trace1IdentifierImage, trace2IdentifierImage,trace3IdentifierImage,trace4IdentifierImage;
    OscilloscopeTrace[] traces;

    public OscilloscopeDisplay() {
        scale = 50d;
        offsetX = 0;
        offsetY = 0;
        try {
            trace1IdentifierImage = ImageIO.read(new File("resources/menuAssets/trace1.png"));
            trace2IdentifierImage = ImageIO.read(new File("resources/menuAssets/trace2.png"));
            trace3IdentifierImage = ImageIO.read(new File("resources/menuAssets/trace3.png"));
            trace4IdentifierImage = ImageIO.read(new File("resources/menuAssets/trace4.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        trace1 = new OscilloscopeTrace(this, TRACE_1_COLOR, trace1IdentifierImage);
        trace2 = new OscilloscopeTrace(this, TRACE_2_COLOR, trace2IdentifierImage);
        trace2.isVisible = false;
        trace3 = new OscilloscopeTrace(this, TRACE_3_COLOR, trace3IdentifierImage);
        trace3.isVisible = false;
        trace4 = new OscilloscopeTrace(this, TRACE_4_COLOR, trace4IdentifierImage);
        trace4.isVisible = false;

        traces = new OscilloscopeTrace[] {trace1, trace2, trace3, trace4};

        setSelectedTrace(1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(BACKGROUND_GRID_COLOR);

        int gridSize = (int) scale;
        int centerX = leftMargin + (getWidth() - leftMargin) / 2;
        int centerY = getHeight() / 2;

        // draw vertical grid lines centered at centerX
        for (int x = centerX; x <= getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
            int mirrorX = centerX - (x - centerX);
            if (mirrorX >= leftMargin) g2d.drawLine(mirrorX, 0, mirrorX, getHeight());
        }

        // draw horizontal grid lines centered at centerY
        for (int y = centerY; y <= getHeight(); y += gridSize) {
            g2d.drawLine(leftMargin, y, getWidth(), y);
            int mirrorY = centerY - (y - centerY);
            if (mirrorY >= 0) g2d.drawLine(leftMargin, mirrorY, getWidth(), mirrorY);
        }

        int tickWidth = 6;
        double tickFreq = .2;

        // Horizontal ticks
        for (int y = centerY; y <= getHeight(); y += (int) (gridSize*tickFreq)) {
            g2d.drawLine(centerX - tickWidth, y, centerX + tickWidth, y);
            int mirrorY = centerY - (y - centerY);
            if (mirrorY >= 0)
                g2d.drawLine(centerX - tickWidth, mirrorY, centerX + tickWidth, mirrorY);
        }

        // Vertical ticks
        for (int x = centerX; x <= getWidth(); x += (int) (gridSize*tickFreq)) {
            g2d.drawLine(x, centerY - tickWidth, x, centerY + tickWidth);
            int mirrorX = centerX - (x - centerX);
            if (mirrorX >= leftMargin)
                g2d.drawLine(mirrorX, centerY - tickWidth, mirrorX, centerY + tickWidth);
        }

        for (OscilloscopeTrace t : traces) {
            if (t != currentTrace) {
                t.renderTrace(g2d);
            }
        }
        if (currentTrace != null) {
            currentTrace.renderTrace(g2d);
        }


        // center crosshair
        g2d.setColor(BACKGROUND_GRID_COLOR);
        //
        // g2d.setStroke(new BasicStroke(1f));
        g2d.drawLine(leftMargin, centerY, getWidth(), centerY);
        g2d.drawLine(centerX, 0, centerX, getHeight());
    }

    public void setZoom(double v) {
        scale = v;
    }

    public double getVoltsPerDiv(int i) {
        return scale;
    }

    public void setTimeScale(double value) {
        timeScale = value;
    }

    public void setHorizontalShift(double value) {
        if (currentTrace!= null)
            currentTrace.horizShift = value;
    }

    public void setVerticalShift(double value) {
        if (currentTrace!= null)
            currentTrace.vertShift = value;
    }


    public double getTimeScale() {
        return timeScale;
    }

    public double getTriggerLevel() {
        return 0;
    }

    public void setSelectedTrace(int traceNum) {
        currentTrace = traces[traceNum-1];
        currentTraceNumber = traceNum;
    }

    public OscilloscopeTrace getTrace(int traceNum) {
        return traces[traceNum-1];
    }

    public void setTraceVisible(int traceNum, boolean setVisible) {
        traces[traceNum-1].isVisible = setVisible;
    }

    public void setWaveform(String waveform, double freq, double amp, double offset, int traceNum) {
        this.traces[traceNum-1].setFunction(waveform, freq, amp, offset, getWidth(), 1);
    }
}
