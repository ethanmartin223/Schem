package Editor;

import javax.swing.*;
import java.awt.*;

public class EditorBottomTaskbar extends JPanel {


    private Label cursorPosLabel;
    private double cursorX =0, cursorY =0;
    private EditorArea editorArea;

    public EditorBottomTaskbar(EditorArea mainEditor) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(-1,30));
        editorArea = mainEditor;

        cursorPosLabel = new Label("  x= "+cursorX+" y= "+cursorY);

        add(cursorPosLabel, BorderLayout.WEST);
    }

    public void updateCursorPosReadout(double x, double y) {
        cursorX = x;
        cursorY = y;
        cursorPosLabel.setText("  x= "+String.format("%.2f", x)+" y= "+String.format("%.2f", y));
        revalidate();
    }

}
