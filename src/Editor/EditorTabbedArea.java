package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorTabbedArea extends JFrame {

    private final JTabbedPane tabbedPane;

    public EditorTabbedArea() {
        tabbedPane = new JTabbedPane();

        addTab("Welcome Tab");

        JButton addButton = new JButton("➕ New Tab");
        addButton.addActionListener(e -> addTab("Tab " + (tabbedPane.getTabCount() + 1)));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(addButton);

        add(controlPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addTab(String title) {
        JPanel content = new JPanel(new BorderLayout());
        content.add(new EditorArea(this));

        tabbedPane.addTab(title, content);

        int index = tabbedPane.indexOfComponent(content);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        tabHeader.setOpaque(false);

        JLabel tabLabel = new JLabel(title);
        JButton closeButton = new JButton("×");
        closeButton.setMargin(new Insets(0, 5, 0, 5));
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFont(new Font("SansSerif", Font.PLAIN, 24));
        closeButton.setForeground(Color.BLACK);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int closingIndex = tabbedPane.indexOfComponent(content);
                if (closingIndex != -1) {
                    tabbedPane.remove(closingIndex);
                }
            }
        });

        tabHeader.add(tabLabel);
        tabHeader.add(closeButton);
        tabbedPane.setTabComponentAt(index, tabHeader);
    }
}
