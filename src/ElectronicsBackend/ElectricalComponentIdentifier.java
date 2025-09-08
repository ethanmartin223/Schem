package ElectronicsBackend;

import ElectricalComponents.WireNode;
import ElectricalComponents.*;

public enum ElectricalComponentIdentifier {
    DIODE("diode", Diode.class, "Diodes"),
    ZENER_DIODE("zenerdiode", ZenerDiode.class, "Diodes"),

    NPN_TRANSISTOR("npntransistor", NpnTransistor.class, "Transistors"),
    PNP_TRANSISTOR("pnptransistor", PnpTransistor.class, "Transistors"),

    AND_GATE("and", ANDGate.class, "Logic Gates"),
    OR_GATE("or", ORGate.class, "Logic Gates"),
    XOR_GATE("xor", XORGate.class, "Logic Gates"),
    NAND_GATE("nand", NANDGate.class, "Logic Gates"),

    RESISTOR("resistor", Resistor.class, "Miscellaneous"),
    VARIABLE_RESISTOR("variableresistor", VariableResistor.class, "Miscellaneous"),
    LAMP("lamp", Lamp.class, "Miscellaneous"),
    MICROPHONE("microphone", Microphone.class, "Miscellaneous"),
    TRANSFORMER("transformer", Transformer.class, "Miscellaneous"),
    PHOTORESISTOR("photoresistor", Photoresistor.class, "Miscellaneous"),
    CAPACITOR("capacitor", Capacitor.class, "Miscellaneous"),
    SPEAKER("speaker", Speaker.class, "Miscellaneous"),
    INTEGRATED_CIRCUIT("ic", IntegratedCircuit.class, "Miscellaneous"),

    GROUND("ground", Ground.class, "Power Components"),
    POWERSUPPLY("powerSupply", PowerSupply.class, "Power Components"),

    WIRE_NODE("wirenode", WireNode.class, "Wiring");

    public final String id;
    public final Class objectType;
    public final String category;

    ElectricalComponentIdentifier(String label, Class cl, String category) {
        this.id = label;
        this.objectType = cl;
        this.category = category;
    }

    public static Class findClassFromID(String id) {
        for (ElectricalComponentIdentifier eci : ElectricalComponentIdentifier.values()) {
            if (eci.id.equals(id)) {
                return eci.objectType;
            }
        }
        return null;
    }
}
