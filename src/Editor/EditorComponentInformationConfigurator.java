package Editor;

import ElectronicsBackend.ElectricalComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EditorComponentInformationConfigurator extends JPanel {

    private EditorArea mainEditor;

    private ElectricalComponent currentlyDisplayedComponent;
    JPanel currentCard;

    public EditorComponentInformationConfigurator(EditorArea mainEditor) {
        setLayout(new BorderLayout());
        this.mainEditor = mainEditor;
        currentlyDisplayedComponent = null;
        currentCard = null;
    }

    public void setComponent(ElectricalComponent comp) {
        currentlyDisplayedComponent = comp;
        if (currentCard != null) remove(currentCard);
        if (comp!= null) {
            currentCard = comp.getInfoCard();
            add(currentCard);
        }
        revalidate();
        repaint();
    }

    public void setBlank() {
        setComponent(null);
    }


}
