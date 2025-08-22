package Ocilliscope;

import Ocilliscope.FunctionGenerator.FunctionGeneratorWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class OscilloscopeWindow extends JFrame {

    private JLabel triggerLabel = null;
    private JLabel ch1VoltsLabel = null;
    private JLabel ch2VoltsLabel = null;
    private JLabel ch3VoltsLabel = null;
    private JLabel ch4VoltsLabel = null;
    private JLabel timeDivLabel = null;

    protected OscilloscopeKnob timeKnob, zoomKnob, verticalShiftKnob, horizontalShiftKnob;

    OscilloscopeDisplay oc;
    Image knobImg;

    public OscilloscopeWindow() {

        setUIFont(new FontUIResource("Segoe UI", Font.PLAIN, 13));

        try {
            knobImg = ImageIO.read(new File("resources/menuAssets/knob.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        oc = new OscilloscopeDisplay();
        centerPanel.add(oc, BorderLayout.CENTER);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.getWidth() * .6), (int) (screenSize.getHeight() * .5));
        setLocationRelativeTo(null);

        // ----- Knobs -----
        timeKnob = new OscilloscopeKnob(oc, knobImg, 1, 20, 2, 2);
        zoomKnob = new OscilloscopeKnob(oc, knobImg, 5, 100, 50, 2);
        verticalShiftKnob = new OscilloscopeKnob(oc, knobImg, -getHeight(), +getHeight(), 5, 1);
        horizontalShiftKnob = new OscilloscopeKnob(oc, knobImg, 0, getWidth(), 10, 1);

        // ----- Sidebar (RIGHT) -----
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setBorder(new MatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY));

        // Knobs in 2x2 grid
        JPanel knobGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        knobGrid.setOpaque(false);
        knobGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        knobGrid.add(createKnobPanel("TIME BASE", timeKnob,
                "resources/menuAssets/timescale.png",
                () -> {
                    oc.setTimeScale((int) timeKnob.getValue());
                    timeDivLabel.setText("Time/div: " + oc.getTimeScale() + " ms/div");
                    oc.repaint();
                }));

        knobGrid.add(createKnobPanel("ZOOM", zoomKnob,
                "resources/menuAssets/zoomocil.png",
                () -> {
                    oc.setZoom(zoomKnob.getValue());
                    oc.repaint();
                }));

        knobGrid.add(createKnobPanel("VERT SHIFT", verticalShiftKnob,
                "resources/menuAssets/updown.png",
                () -> {
                    oc.setVerticalShift((int) verticalShiftKnob.getValue());
                    oc.repaint();
                }));

        knobGrid.add(createKnobPanel("HORZ SHIFT", horizontalShiftKnob,
                "resources/menuAssets/rightleft.png",
                () -> {
                    oc.setHorizontalShift((int) horizontalShiftKnob.getValue());
                    oc.repaint();
                }));

        sidePanel.add(knobGrid, BorderLayout.NORTH);

        // Trace buttons in a row
        JPanel traceButtonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        traceButtonPanel.setOpaque(false);
        traceButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        class FixedStateButtonModel extends DefaultButtonModel {
            @Override
            public boolean isPressed() { return false; }
            @Override
            public boolean isRollover() { return false; }
            @Override
            public void setRollover(boolean b) { }
        }

        for (int i = 1; i <= 4; i++) {
            AtomicInteger traceNum = new AtomicInteger(i);
            JButton btn = new JButton("Trace " + i);
            btn.setFocusPainted(false);
            btn.setBackground(oc.getTrace(i) == oc.currentTrace ? oc.currentTrace.color : Color.LIGHT_GRAY);
            btn.setForeground(Color.BLACK);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBorder(new EmptyBorder(8, 15, 8, 15));
            btn.setModel(new FixedStateButtonModel());

            btn.addActionListener(e -> {
                OscilloscopeTrace selectedTrace = oc.getTrace(traceNum.get());
                if (oc.currentTrace == selectedTrace && selectedTrace.isVisible) {
                    btn.setBackground(Color.LIGHT_GRAY);
                    oc.setTraceVisible(traceNum.get(), false);
                    oc.currentTrace = null;
//                    for (int r=0; r< oc.traces.length; r++) {
//                        if (oc.getTrace(r+1).isVisible) {
//                            setKnobsToTracePositions(oc.getTrace(r+1));
//                            oc.setSelectedTrace(r+1);
//                            oc.repaint();
//                            traceNum.set(r + 1);
//                            break;
//                        }
//                    }
                } else {
                    oc.setTraceVisible(traceNum.get(), true);
                    btn.setBackground(selectedTrace.color);
                    setKnobsToTracePositions(selectedTrace);
                }

                Border unselectedBorder = new EmptyBorder(5, 10, 5, 10);
                ch1VoltsLabel.setBorder(unselectedBorder);
                ch2VoltsLabel.setBorder(unselectedBorder);
                ch3VoltsLabel.setBorder(unselectedBorder);
                ch4VoltsLabel.setBorder(unselectedBorder);

                if (oc.currentTrace != null) {
                    Border selectedBorder = BorderFactory.createCompoundBorder(
                            new MatteBorder(5, 0, 0, 0, oc.getTrace(traceNum.get()).color),
                            new EmptyBorder(0, 10, 5, 10));
                    switch (traceNum.get()) {
                        case 1 -> ch1VoltsLabel.setBorder(selectedBorder);
                        case 2 -> ch2VoltsLabel.setBorder(selectedBorder);
                        case 3 -> ch3VoltsLabel.setBorder(selectedBorder);
                        case 4 -> ch4VoltsLabel.setBorder(selectedBorder);
                    }
                }
                if (!(oc.currentTrace == selectedTrace && selectedTrace.isVisible))
                    oc.setSelectedTrace(traceNum.get());
                oc.repaint();
            });

            traceButtonPanel.add(btn);
        }

        sidePanel.add(traceButtonPanel, BorderLayout.SOUTH);

        add(sidePanel, BorderLayout.EAST);

        // ----- Bottom Toolbar -----
        JPanel bottomToolbar = new JPanel();
        bottomToolbar.setLayout(new BoxLayout(bottomToolbar, BoxLayout.X_AXIS));
        bottomToolbar.setBackground(Color.WHITE);
        bottomToolbar.setBorder(new MatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY));

        Font voltLabelFont = new FontUIResource("Segoe UI", Font.BOLD, 13);
        ch1VoltsLabel = new JLabel("CH1: " + oc.getVoltsPerDiv(1) + " V/div");
        ch1VoltsLabel.setFont(voltLabelFont);
        ch1VoltsLabel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(5, 0, 0, 0, oc.getTrace(1).color),
                new EmptyBorder(0, 10, 5, 10)));

        ch2VoltsLabel = new JLabel("CH2: " + oc.getVoltsPerDiv(2) + " V/div");
        ch2VoltsLabel.setFont(voltLabelFont);
        ch2VoltsLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        ch3VoltsLabel = new JLabel("CH3: " + oc.getVoltsPerDiv(3) + " V/div");
        ch3VoltsLabel.setFont(voltLabelFont);
        ch3VoltsLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        ch4VoltsLabel = new JLabel("CH4: " + oc.getVoltsPerDiv(4) + " V/div");
        ch4VoltsLabel.setFont(voltLabelFont);
        ch4VoltsLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        bottomToolbar.add(ch1VoltsLabel);
        bottomToolbar.add(Box.createHorizontalStrut(10));
        bottomToolbar.add(ch2VoltsLabel);
        bottomToolbar.add(Box.createHorizontalStrut(10));
        bottomToolbar.add(ch3VoltsLabel);
        bottomToolbar.add(Box.createHorizontalStrut(10));
        bottomToolbar.add(ch4VoltsLabel);

        centerPanel.add(bottomToolbar, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // ----- Top Toolbar -----
        JPanel topToolbar = new JPanel();
        topToolbar.setLayout(new BoxLayout(topToolbar, BoxLayout.X_AXIS));
        topToolbar.setBackground(Color.WHITE);
        topToolbar.setBorder(new MatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));

        timeDivLabel = new JLabel("Time/div: " + oc.getTimeScale() + " ms/div");
        timeDivLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        triggerLabel = new JLabel("Trigger: " + oc.getTriggerLevel() + " V");
        triggerLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        topToolbar.add(timeDivLabel);
        topToolbar.add(Box.createHorizontalStrut(20));
        topToolbar.add(triggerLabel);

        centerPanel.add(topToolbar, BorderLayout.NORTH);

        FunctionGeneratorWindow genWin = new FunctionGeneratorWindow(
                (waveform, freq, amp, offset) -> {
                    oc.setWaveform(waveform, freq, amp, offset,1);
                }
        );
        genWin.setVisible(true);
        setVisible(true);

        oc.setWaveform("sine", 25, 30.0, 0.0, 1);
        oc.setWaveform("triangle", 10, 30.0, 0.0, 2);
        oc.setWaveform("square", 10, 35.0, 0.0, 3);
        oc.setWaveform("sine", 1000, 5.0, 0.0, 4);
    }

    private void setKnobsToTracePositions(OscilloscopeTrace trace) {
        verticalShiftKnob.eventvalue = (trace.vertShift);
        verticalShiftKnob.repaint();

        horizontalShiftKnob.eventvalue = (trace.horizShift);
        horizontalShiftKnob.repaint();
    }

    private JPanel createKnobPanel(String title, OscilloscopeKnob knob, String iconPath, Runnable onChange) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, Color.GRAY),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        JLabel iconLabel = new JLabel(new ImageIcon(iconPath), SwingConstants.CENTER);

        JPanel knobRow = new JPanel(new BorderLayout());
        knobRow.setOpaque(false);
        knobRow.add(knob, BorderLayout.CENTER);
        knobRow.add(iconLabel, BorderLayout.WEST);

        knob.addPropertyChangeListener("value", e -> onChange.run());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(knobRow, BorderLayout.CENTER);
        return panel;
    }

    public static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        new OscilloscopeWindow();

    }
}
