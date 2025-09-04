package Application;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ApplicationSettingsWindow extends JFrame {

    private JTree categoryTree;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Map<String, JScrollPane> pages = new HashMap<String, JScrollPane>();

    public ApplicationSettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);

        // ---- Left Sidebar (Tree) ----
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Settings");
        DefaultMutableTreeNode appearance = new DefaultMutableTreeNode("Appearance & Behavior");
        appearance.add(new DefaultMutableTreeNode("Appearance"));
        appearance.add(new DefaultMutableTreeNode("Graphics Settings"));

        DefaultMutableTreeNode simulation = new DefaultMutableTreeNode("Simulation Settings");
        simulation.add(new DefaultMutableTreeNode("Simulation Quality"));

        root.add(appearance);
        root.add(simulation);

        categoryTree = new JTree(root);
        categoryTree.setRootVisible(false);
        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (node == null || !node.isLeaf()) return;
            String key = node.toString();
            if (pages.containsKey(key)) {
                cardLayout.show(contentPanel, key);
            }
        });

        JScrollPane treeScroll = new JScrollPane(categoryTree);
        treeScroll.setPreferredSize(new Dimension(250, 600));
        treeScroll.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- Right Content (CardLayout) ----
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Add pages
        addPage("Appearance", createAppearancePanel());
        addPage("Graphics Settings", createGraphicsPanel());
        addPage("Simulation Quality", placeholderPanel("Simulation settings here..."));

        // Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, contentPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }


    // ---------------- PAGE CREATION ---------------- //
    private void addPage(String name, JScrollPane panel) {
        pages.put(name, panel);
        contentPanel.add(panel, name);
    }

    private JScrollPane createGraphicsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        int row = 0;
        row = addSectionDivider(p, "Rendering Settings", row);
        row = addDropdownEntry(p, "Rendering Quality:",
                new String[]{"Speed", "Quality", "Default"}, row);
        row = addDropdownEntry(p, "Dithering:",
                new String[]{"Enabled", "Disabled", "Default"}, row);
        row = addDropdownEntry(p, "Color Rendering Quality:",
                new String[]{"Speed", "Quality", "Default"}, row);
        row = addDropdownEntry(p, "Transparency Rendering Quality:",
                new String[]{"Speed", "Quality", "Default"}, row);
        row = addDropdownEntry(p, "Antialiasing:",
                new String[]{"On", "Off", "Default"}, row);
        row = addDropdownEntry(p, "Interpolation:",
                new String[]{"Nearest Neighbor", "Bilinear", "Bicubic"}, row);

        row = addSectionDivider(p, "Video Quality Settings", row);
        row = addCheckboxEntry(p, "Wait for Vertical Sync (vsync):", row);
        row = addSliderEntry(p, "Maximum Frame Rate", 60, 360, 120, row);

        JScrollPane sp = new JScrollPane(p);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getVerticalScrollBar().setUnitIncrement(4);
        sp.setBorder(null);
        return sp;
    }

    private JScrollPane createAppearancePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        int row = 0;
        row = addSectionDivider(p, "UI Appearance Settings", row);
        row = addDropdownEntry(p, "Theme:", new String[]{"Light", "Dark", "System"}, row);
        row = addDropdownEntry(p, "Zoom:", new String[]{"100%", "110%", "120%"}, row);
        row = addCheckboxEntry(p, "Use custom font", row);
        row = addDropdownEntry(p, "Font:", new String[]{"Inter", "Arial", "Segoe UI"}, row);
        row = addSliderEntry(p, "Size:", 8, 36, 12, row);

        JScrollPane sp = new JScrollPane(p);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getVerticalScrollBar().setUnitIncrement(4);
        sp.setBorder(null);
        return new JScrollPane(p);
    }

    private JScrollPane placeholderPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return new JScrollPane(p);
    }

    // ---------------- ENTRY HELPERS ---------------- //

    // Section divider helper
    private int addSectionDivider(JPanel panel, String title, int row) {
        GridBagConstraints gbc = baseConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        if (title != null && !title.isEmpty()) {
            JLabel header = new JLabel(title);
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            panel.add(header, gbc);
            row++;
            gbc.gridy = row;
        }

        JSeparator sep = new JSeparator();
        panel.add(sep, gbc);
        return row + 1;
    }

    // Generic entry (label + component)
    private int addEntryToPage(JPanel panel, String label, JComponent comp, int row) {
        GridBagConstraints gbc = baseConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;

        if (label != null && !label.isEmpty()) {
            JLabel n = new JLabel(label);
            n.setBorder(new EmptyBorder(0,30,0,0));
            panel.add(n, gbc);
            gbc.gridx = 1;
        } else {
            gbc.gridx = 0;
            gbc.gridwidth = 2;
        }

        panel.add(comp, gbc);
        return row + 1;
    }

    // Dropdown helper
    private int addDropdownEntry(JPanel panel, String label, String[] options, int row) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setFocusable(false);
        comboBox.setPreferredSize(new Dimension(150, 25));
        return addEntryToPage(panel, label, comboBox, row);
    }

    // Checkbox helper
    private int addCheckboxEntry(JPanel panel, String text, int row) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFocusable(false);
        checkBox.setBorder(new EmptyBorder(0,30,0,0));
        return addEntryToPage(panel, "", checkBox, row);
    }

    // Slider helper
    private int addSliderEntry(JPanel panel, String label, int min, int max, int initial, int row) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setMajorTickSpacing((max - min) / 4);
        slider.setPaintTicks(true);
        slider.setFocusable(false);
        slider.setPaintLabels(true);
        return addEntryToPage(panel, label, slider, row);
    }

    // ---------------- UTIL ---------------- //

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    // ---------------- DEMO ---------------- //

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new ApplicationSettingsWindow().setVisible(true));
    }
}
