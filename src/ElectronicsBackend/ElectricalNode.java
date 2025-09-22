package ElectronicsBackend;

import Editor.Wire;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class ElectricalNode {
    final Set<ElectricalComponent> components = new HashSet<>();
    final Set<Wire> wires = new HashSet<>();

    static Color[] availableColors = new Color[] {
            Color.RED,                       // bright red
            new Color(0, 153, 0),            // vibrant green
            new Color(0, 153, 204),          // teal / cyan
            new Color(204, 0, 153),          // magenta / fuchsia
            new Color(255, 102, 0),          // vivid orange
            new Color(0, 102, 204),          // royal blue
            new Color(102, 0, 204),          // deep purple
            new Color(0, 204, 102),          // bright sea green
            new Color(255, 51, 153),         // hot pink
            new Color(204, 51, 0),           // deep orange / brick
            new Color(51, 102, 204),         // steel blue
            new Color(0, 51, 102),           // navy (very high contrast)
            new Color(153, 51, 102),         // wine / maroon
            new Color(139, 69, 19)           // brown (good for distinction)
    };


    int id=0;
    Color color;

    public ElectricalNode(int nodeNum) {
        id=nodeNum;
        color = availableColors[(int) (Math.random()* availableColors.length)];
    }

    public void addChild(ElectricalComponent c) {
        components.add(c);
    }

    public void addWire(Wire w) {
        wires.add(w);
    }

    public Set<ElectricalComponent> getComponents() {
        return components;
    }

    public Set<Wire> getWires() {
        return wires;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Node(\nConnects to:\n");
        for (ElectricalComponent c : components) {
            sb.append("- ").append(c).append("\n");
        }
        sb.append("Wires:\n");
        for (Wire w : wires) {
            sb.append("- ").append(w).append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return components.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElectricalNode other)) return false;
        return components.equals(other.components);
    }

    public double getResistance() {
        double sum =0;
        for (ElectricalComponent eC : components) {
            if (eC.getResistance()>0)
                sum+=1.0/eC.getResistance();
        }
        return sum;
    }
}