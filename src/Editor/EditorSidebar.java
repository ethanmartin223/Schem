package Editor;

import ElectronicsBackend.ElectricalComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class EditorSidebar extends JPanel {

    private final EditorArea mainEditor;
    private final JPanel componentList;

    private final Color BG_LIGHT = Color.WHITE;
    private final Color ITEM_HOVER = new Color(255, 190, 0);
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

        // Simply iterate through all components without grouping
        for (Class<?> c : ElectricalComponent.subclasses) {
            componentList.add(createListItem(c));
        }
    }

    private JPanel createListItem(Class<?> clazz) {
        String id;
        try {
            // Assume each class has a public static field `id`
            id = (String) clazz.getField("id").get(null);
        } catch (Exception e) {
            id = clazz.getSimpleName(); // fallback
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel iconLabel;
        try {
            Image scaled = ImageIO.read(new File("resources/" + id + ".png"))
                    .getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaled));
        } catch (Exception e) {
            iconLabel = new JLabel("•");
        }

        JLabel textLabel = new JLabel(id);
        textLabel.setFont(ITEM_FONT);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textLabel, BorderLayout.CENTER);

        String finalId = id;
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
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (finalId.equals("Wire")) {
                    mainEditor.enableWireMode();
                } else {
                    mainEditor.setCreatingNewComponent(finalId);
                }
            }
        });

        return panel;
    }

    private void addWireButton() {
        componentList.add(createListItem("Wire"));
    }

    // Overloaded version to support Wire button
    private JPanel createListItem(String id) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel iconLabel;
        try {
            Image scaled = ImageIO.read(new File("resources/" + id + ".png"))
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH);
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
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (id.equals("Wire")) {
                    mainEditor.enableWireMode();
                } else {
                    mainEditor.setCreatingNewComponent(id);
                }
            }
        });

        return panel;
    }
}
