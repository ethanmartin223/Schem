package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorTopMenuBar extends JMenuBar {

    public EditorTopMenuBar() {

        Font segoe = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Menu.font", segoe);
        UIManager.put("MenuItem.font", segoe);

        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: implement new project action
                System.out.println("New project created");
            }
        });

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: implement open file action
                System.out.println("Open file dialog");
            }
        });


        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: implement save action
                System.out.println("Project saved");
            }
        });


        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });


        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ===== EDIT MENU =====
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: implement undo
                System.out.println("Undo action");
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: implement redo
                System.out.println("Redo action");
            }
        });

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: open settings dialog
                System.out.println("Open settings dialog");
            }
        });

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(settingsItem);

        JMenu viewMenu = new JMenu("View");

        JMenu navigateMenu = new JMenu("Navigate");

        JMenu runMenu = new JMenu("Run");

        JMenu graphicsMenu = new JMenu("Graphics Settings");

        JMenu helpMenu = new JMenu("Help");

        JMenu aboutMenu = new JMenu("About");




        // ===== ADD MENUS TO BAR =====
        this.add(fileMenu);
        this.add(editMenu);
        this.add(navigateMenu);
        this.add(runMenu);
        this.add(graphicsMenu);
        this.add(viewMenu);
        this.add(helpMenu);
        this.add(aboutMenu);

    }
}
