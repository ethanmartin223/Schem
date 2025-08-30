package Editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class EditorQuickEntryField extends JPanel {

    static String[] autoCompleteOptionStrings = new String[] {
            "diode", "zenerdiode", "npntransistor", "pnptransistor",
            "and", "or", "xor", "nand",
            "resistor", "variableresistor", "lamp", "microphone", "transformer", "photoresistor",
            "ground", "powerSupply", "wirenode",
            "capacitor", "variablecapacitor", "inductor", "fuse", "led", "switch", "pushbutton",
            "buzzer", "opamp", "voltageRegulator", "thermistor", "photodiode", "triac", "relay",
            "speaker", "antenna", "coil", "motor", "crystalOscillator", "potentiometer", "currentSensor",
            "diac", "varistor", "hallSensor", "lcd", "sevenSegmentDisplay", "toggleSwitch", "reedSwitch",
            "rotaryEncoder", "solenoid", "temperatureSensor", "humiditySensor", "pressureSensor",
            "magnet", "capacitiveTouch", "microcontroller",
            "dcMotor", "stepperMotor", "servoMotor", "bipolarCapacitor", "piezoElement"
    };

    JTextField entry;
    JList<String> suggestionList;
    DefaultListModel<String> listModel;
    JScrollPane scrollPane;
    EditorArea editor;

    private static final int MAX_ROWS = 8;

    public EditorQuickEntryField(EditorArea mainEditor) {
        editor = mainEditor;

        entry = new JTextField(12);

        listModel = new DefaultListModel<>();
        for (String s : autoCompleteOptionStrings) listModel.addElement(s);

        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(5);
        suggestionList.setPrototypeCellValue("microcontroller________");

        scrollPane = new JScrollPane(suggestionList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        entry.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculateAutoCompleteOptions(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculateAutoCompleteOptions(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        setLayout(new BorderLayout());
        setBorder(null);
        add(entry, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        entry.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (!listModel.isEmpty() && suggestionList.getSelectedIndex() >= 0) {
                            String data = suggestionList.getModel().getElementAt(suggestionList.getSelectedIndex());
                            editor.setCreatingNewComponent(data);
                            editor.repaint();
                        }
                        setVisible(false);
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        setVisible(false);
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int i = suggestionList.getSelectedIndex();
                        if (i < listModel.size() - 1) suggestionList.setSelectedIndex(i + 1);
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int i = suggestionList.getSelectedIndex();
                        if (i > 0) suggestionList.setSelectedIndex(i - 1);
                    }
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                entry.grabFocus();
                if (entry.getText().length()>1) entry.setText(entry.getText().substring(0,1));
            }
            @Override public void focusLost(FocusEvent e) {
            }
        });

        entry.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
            }
            @Override public void focusLost(FocusEvent e) {
                setVisible(false);
                mainEditor.grabFocus();
            }
        });
    }

    private void recalculateAutoCompleteOptions() {
        String text = entry.getText().toLowerCase();
        listModel.clear();
        if (text.isEmpty()) {
            setVisible(false);
            return;
        }
        for (String o : autoCompleteOptionStrings) {
            if (o.toLowerCase().startsWith(text)) {
                listModel.addElement(o);
            }
        }
        for (String o : autoCompleteOptionStrings) {
            String lo = o.toLowerCase();
            if (lo.contains(text) && !listModel.contains(o)) {
                listModel.addElement(o);
            }
        }

        if (listModel.isEmpty()) {
            scrollPane.setVisible(false);
            applyMinimalSize();
        } else {
            scrollPane.setVisible(true);
            suggestionList.setSelectedIndex(0);
            applyDynamicSize();
        }

        if (!isVisible()) setVisible(true);
    }

    private void applyMinimalSize() {
        Dimension entrySize = entry.getPreferredSize();
        setBounds(getX(), getY(), entrySize.width, entrySize.height);
        revalidate();
        repaint();
    }

    private void applyDynamicSize() {
        int rows = Math.min(listModel.size(), MAX_ROWS);
        if (rows <= 0) rows = 1;

        int itemHeight = suggestionList.getFixedCellHeight();
        if (itemHeight <= 0) {
            Rectangle r = suggestionList.getCellBounds(0, 0);
            itemHeight = (r != null ? r.height : entry.getFontMetrics(entry.getFont()).getHeight() + 6);
        }

        suggestionList.setVisibleRowCount(rows);

        Dimension entrySize = entry.getPreferredSize();
        Dimension listPref = suggestionList.getPreferredSize();
        int listHeight = rows * itemHeight;
        int totalWidth = Math.max(entrySize.width, listPref.width);
        int totalHeight = entrySize.height + listHeight;
        Dimension viewportSize = new Dimension(totalWidth, listHeight);
        scrollPane.setPreferredSize(viewportSize);
        scrollPane.setMinimumSize(viewportSize);
        setBounds(getX(), getY(), totalWidth, totalHeight);

        revalidate();
        repaint();
        Container p = getParent();
        if (p != null) {
            p.revalidate();
            p.repaint();
        }
    }

    public void setText(String s) { entry.setText(s); }
    public void setCaretPosition(int i) { entry.setCaretPosition(i); }
}