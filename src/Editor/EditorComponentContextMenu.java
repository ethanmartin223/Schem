package Editor;

import javax.swing.*;

class EditorComponentContextMenu extends JPopupMenu {
    JMenuItem anItem,anotherItem;

    public EditorComponentContextMenu() {
        anItem = new JMenuItem("Test Editor Options");
        add(anItem);

        anotherItem = new JMenuItem("More Options");
        add(anotherItem);
    }
}