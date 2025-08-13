package Editor.History;

import Editor.EditorArea;
import Editor.EditorHistoryTrackerList;

import java.util.ArrayList;

import static java.util.Arrays.stream;

public class History {

    ArrayList<HistoryEntry> pastHistoryEntries;
    ArrayList<HistoryEntry> futureHistoryEntries;

    EditorHistoryTrackerList editorHistoryList;

    EditorArea mainEditor;

    public History(EditorArea editorArea) {
        mainEditor = editorArea;
        pastHistoryEntries = new ArrayList<>();
        futureHistoryEntries = new ArrayList<>();
    }


    public void addEvent(Event ev, double x, double  y, Object component) {
        futureHistoryEntries.clear();

        HistoryEntry entry = new HistoryEntry();
        entry.editorScale = mainEditor.scale;
        entry.editorXPosition = mainEditor.xPosition;
        entry.editorYPosition = mainEditor.yPosition;
        entry.editorPressScreenX = mainEditor.pressScreenX;
        entry.editorPressScreenY = mainEditor.pressScreenY;
        entry.editorWireStartComponent = mainEditor.wireStartComponent;
        entry.editorWireStartIndex = mainEditor.wireStartIndex;
        entry.editorInWireMode = mainEditor.inWireMode;
        entry.editorLastReleasedPositionX = mainEditor.lastReleasedPositionX;
        entry.editorLastReleasedPositionY = mainEditor.lastReleasedPositionY;
        entry.editorCreatingNewComponent = mainEditor.creatingNewComponent;
        entry.editorCreatingComponentID = mainEditor.creatingComponentID;
        entry.editorXPress = mainEditor.xPress;
        entry.editorYPress = mainEditor.yPress;
        entry.editorXDrag = mainEditor.xDrag;
        entry.editorYDrag = mainEditor.yDrag;

        entry.event = ev;
        entry.editLocationX = x;
        entry.editLocationY = y;

        entry.component = component;
        entry.componentType = component.getClass();
        editorHistoryList.addEntry(ev+" " +entry.componentType.getCanonicalName());
        pastHistoryEntries.add(entry);
    }


    public void loadEvent(HistoryEntry entry) {
        mainEditor.scale = entry.editorScale;
        mainEditor.xPosition = entry.editorXPosition;
        mainEditor.yPosition = entry.editorYPosition;
        mainEditor.pressScreenX = entry.editorPressScreenX;
        mainEditor.pressScreenY = entry.editorPressScreenY;
        mainEditor.wireStartComponent = entry.editorWireStartComponent;
        mainEditor.wireStartIndex = entry.editorWireStartIndex;
        mainEditor.inWireMode = entry.editorInWireMode;
        mainEditor.lastReleasedPositionX = entry.editorLastReleasedPositionX;
        mainEditor.lastReleasedPositionY = entry.editorLastReleasedPositionY;
        mainEditor.creatingNewComponent = entry.editorCreatingNewComponent;
        mainEditor.creatingComponentID = entry.editorCreatingComponentID;
        mainEditor.xPress = entry.editorXPress;
        mainEditor.yPress = entry.editorYPress;
        mainEditor.xDrag = entry.editorXDrag;
        mainEditor.yDrag = entry.editorYDrag;

    }

    public HistoryEntry getLastAndRemove() {
        if (!pastHistoryEntries.isEmpty()) {
            HistoryEntry hE = pastHistoryEntries.getLast();
            pastHistoryEntries.remove(hE);
            futureHistoryEntries.add(hE);
            editorHistoryList.removeLast();
            return hE;
        } else {
            return null;
        }
    }

    public HistoryEntry getFutureAndRemove() {
        if (!futureHistoryEntries.isEmpty()) {
            HistoryEntry hE = futureHistoryEntries.getLast();
            futureHistoryEntries.remove(hE);
            pastHistoryEntries.add(hE);
            return hE;
        } else {
            return null;
        }
    }

    public void setEditorHistoryList(EditorHistoryTrackerList editorHistoryList) {
        this.editorHistoryList = editorHistoryList;
    }

    public HistoryEntry getLastFromFuture() {
        return futureHistoryEntries.getFirst();
    }


    public static enum Event {
        CREATED_NEW_COMPONENT,
        DELETED_COMPONENT,
        ROTATED_COMPONENT,
        DELETED_WIRE,
        CREATED_WIRE,
        DELETED_CONNECTING_WIRES, MOVED_COMPONENT,
    };


}
