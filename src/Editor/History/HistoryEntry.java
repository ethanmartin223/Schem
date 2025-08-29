package Editor.History;

// ---------------------- // Imports // ---------------------- //
import ElectronicsBackend.ElectricalComponent;

// ---------------------- // History Entry // ---------------------- //
public class HistoryEntry {

    // ---- public ---- //
    public double editLocationX;
    public double editLocationY;

    public History.Event event;
    public Class componentType;
    public Object component;
    public int rotation;

    // ---- private ---- //
    double editorXPosition;
    double editorYPosition;
    double editorLastReleasedPositionX = 0;
    double editorLastReleasedPositionY = 0;
    double editorScale;

    int editorPressScreenX;
    int editorPressScreenY;
    int editorWireStartIndex = 0;
    int editorXPress = 0;
    int editorYPress = 0;
    int editorXDrag = 0;
    int editorYDrag = 0;

    String editorCreatingComponentID;
    ElectricalComponent editorWireStartComponent = null;

    boolean editorInWireMode = false;
    boolean editorCreatingNewComponent;

    // ---------------------- // Constructor // ---------------------- //
    public HistoryEntry() {

    }

}
