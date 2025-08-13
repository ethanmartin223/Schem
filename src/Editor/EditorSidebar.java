package Editor;

import ElectronicsBackend.ElectricalComponentIdentifier;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditorSidebar extends JPanel {

    EditorArea mainEditor;
    JPanel componentList;

    private final Color BG_LIGHT = Color.WHITE;
    private final Color CATEGORY_GRAY = new Color(80, 80, 80);
    private final Color ITEM_HOVER = new Color(255, 190, 0);
    private final Font CATEGORY_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font ITEM_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public EditorSidebar(EditorArea mainEditor) {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        this.mainEditor = mainEditor;
        componentList = new JPanel();
        componentList.setLayout(new BoxLayout(componentList, BoxLayout.Y_AXIS));
        componentList.setBackground(BG_LIGHT);

        JScrollPane scrollPane = new JScrollPane(componentList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_LIGHT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        addWireButton();

        Map<String, List<ElectricalComponentIdentifier>> grouped = new LinkedHashMap<>();
        for (ElectricalComponentIdentifier c : ElectricalComponentIdentifier.values()) {
            grouped.computeIfAbsent(c.category, k -> new ArrayList<>()).add(c);
        }

        for (Map.Entry<String, List<ElectricalComponentIdentifier>> entry : grouped.entrySet()) {
            addCategory(entry.getKey(), entry.getValue());
        }
    }

    private void addCategory(String title, List<ElectricalComponentIdentifier> components) {
        JLabel categoryLabel = new JLabel(title);
        categoryLabel.setFont(CATEGORY_FONT);
        categoryLabel.setForeground(CATEGORY_GRAY);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        componentList.add(categoryLabel);

        for (ElectricalComponentIdentifier c : components) {
            componentList.add(createListItem(c.id));
        }
    }

    private JPanel createListItem(String id) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel iconLabel;
        try {
            ImageIcon icon = new ImageIcon("resources/" + id + ".png");
            Image scaled = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaled));
        } catch (Exception e) {
            iconLabel = new JLabel("•");
        }

        JLabel textLabel = new JLabel(id);
        textLabel.setFont(ITEM_FONT);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textLabel, BorderLayout.CENTER);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(ITEM_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(BG_LIGHT);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (id.equals("Wire")) {
                    mainEditor.enableWireMode();
                }
                else {
                    mainEditor.setCreatingNewComponent(id);
                }
            }
        });

        return panel;
    }

    private void addWireButton() {
        JLabel wireLabel = new JLabel("Wiring");
        wireLabel.setFont(CATEGORY_FONT);
        wireLabel.setForeground(CATEGORY_GRAY);
        wireLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        componentList.add(wireLabel);

        componentList.add(createListItem("Wire"));
    }
}
