package Editor.History;

import Editor.Wire;
import ElectronicsBackend.ElectricalComponent;

import java.util.ArrayList;
import java.util.List;

public class HistoryEntry {
    public double editLocationX, editLocationY;
    public Class componentType;
    public History.Event event;
    public Object component;
    double editorScale;
    double editorXPosition;
    double editorYPosition;
    int editorPressScreenX;
    int editorPressScreenY;
    public List<Wire> editorWires = new ArrayList<>();

    ElectricalComponent editorWireStartComponent = null;
    int editorWireStartIndex = 0;
    boolean editorInWireMode = false;

    double editorLastReleasedPositionX = 0;
    double editorLastReleasedPositionY = 0;

    boolean editorCreatingNewComponent;
    String editorCreatingComponentID;

    int editorXPress = 0;
    int editorYPress = 0;
    int editorXDrag = 0;
    int editorYDrag = 0;


    public HistoryEntry() {
    }

}
