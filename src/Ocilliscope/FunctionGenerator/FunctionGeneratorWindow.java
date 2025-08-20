package Ocilliscope.FunctionGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FunctionGeneratorWindow extends JFrame {

    private JComboBox<String> waveformTypeBox;
    private JSpinner frequencySpinner;
    private JSpinner amplitudeSpinner;
    private JSpinner offsetSpinner;
    private JButton applyButton;

    // Interface with oscilloscope (hook this up to your display logic)
    private FunctionGeneratorListener listener;

    public FunctionGeneratorWindow(FunctionGeneratorListener listener) {
        super("Function Generator");
        this.listener = listener;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Waveform type
        panel.add(new JLabel("Waveform:"));
        waveformTypeBox = new JComboBox<>(new String[]{"Sine", "Square", "Triangle"});
        panel.add(waveformTypeBox);

        // Frequency
        panel.add(new JLabel("Frequency (Hz):"));
        frequencySpinner = new JSpinner(new SpinnerNumberModel(1000.0, 0.1, 1_000_000.0, 1.0));
        panel.add(frequencySpinner);

        // Amplitude
        panel.add(new JLabel("Amplitude (V):"));
        amplitudeSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 1000.0, 0.1));
        panel.add(amplitudeSpinner);

        // Offset
        panel.add(new JLabel("Offset (V):"));
        offsetSpinner = new JSpinner(new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.1));
        panel.add(offsetSpinner);

        // Apply button
        applyButton = new JButton("Apply");
        panel.add(new JLabel()); // spacer
        panel.add(applyButton);

        add(panel);

        // Apply button listener
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String waveform = (String) waveformTypeBox.getSelectedItem();
                double freq = (double) frequencySpinner.getValue();
                double amp = (double) amplitudeSpinner.getValue();
                double offset = (double) offsetSpinner.getValue();

                if (listener != null) {
                    listener.onWaveformUpdate(waveform, freq, amp, offset);
                }
            }
        });
    }

    // Listener interface so oscilloscope can react to function generator changes
    public interface FunctionGeneratorListener {
        void onWaveformUpdate(String waveform, double frequency, double amplitude, double offset);
    }

    // Example standalone usage
    public static void main(String[] args) {
        FunctionGeneratorWindow win = new FunctionGeneratorWindow(
                (waveform, freq, amp, offset) -> {
                    System.out.println("Generated: " + waveform +
                            " Freq=" + freq + "Hz Amp=" + amp + "V Offset=" + offset + "V");
                }
        );
        win.setVisible(true);
    }
}
