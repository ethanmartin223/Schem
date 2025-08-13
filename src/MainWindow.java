import Editor.*;
import ElectronicsBackend.ElectricalComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.util.Enumeration;

public class MainWindow extends JFrame {

    public MainWindow() {
        setLayout(new BorderLayout());

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screenSize.getWidth()*.95),(int)(screenSize.getHeight()*.95));
        setLocationRelativeTo(null);

        setUIFont (new FontUIResource("Segoe UI",Font.PLAIN,13));

        setTitle("WireWorks V1.0 - New Project");
        try {
            Image image = ImageIO.read(new File("resources/generalAssets/WireWorksIcon.png"));
            setIconImage(image);
        } catch (Exception ignored) {
        }

        setJMenuBar(new EditorTopMenuBar());

        EditorArea mainEditor = new EditorArea();
        EditorSidebar sidebar = new EditorSidebar(mainEditor);

        EditorTopToolBar editorToolbar = new EditorTopToolBar(mainEditor);
        add(editorToolbar, BorderLayout.NORTH);

        EditorHistoryTrackerList editorHistoryList= new EditorHistoryTrackerList();
        mainEditor.setEditorHistoryList(editorHistoryList);

        EditorComponentInformationConfigurator detailsPane = new EditorComponentInformationConfigurator(mainEditor);
        mainEditor.setInformationConfigurator(detailsPane); // allow access to sidebar from within maineditor

        EditorBottomTaskbar taskbar = new EditorBottomTaskbar(mainEditor);
        mainEditor.setTaskBar(taskbar); // for coordinate tracking purposes
        add(taskbar, BorderLayout.SOUTH);

        JSplitPane leftSidePanelSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailsPane, editorHistoryList);
        leftSidePanelSplitPane.setOneTouchExpandable(true);
        leftSidePanelSplitPane.setResizeWeight(.5);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSidePanelSplitPane, mainEditor);
        sp.setOneTouchExpandable(true);
        sp.setResizeWeight(0.25);
        JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, sidebar);
        sp2.setOneTouchExpandable(true);
        sp2.setResizeWeight(0.93);
        add(sp2, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void setUIFont (FontUIResource f){
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof FontUIResource) {
                UIManager.put (key, f);
            }
        }
    }


    public static void main(String[] args) {
        MainWindowSplashScreen splash = new MainWindowSplashScreen("resources/generalAssets/wireworkssplash.png");

        //do loading shit after splash is already up
        System.setProperty("sun.java2d.opengl", "true");
        MainWindow mw = new MainWindow();

        splash.setVisible(false);

    }
}
