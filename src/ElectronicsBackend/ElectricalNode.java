package ElectronicsBackend;

import Editor.Wire;
import ElectronicsBackend.ElectricalComponent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class ElectricalNode {
    final Set<ElectricalComponent> components = new HashSet<>();
    final Set<Wire> wires = new HashSet<>();

    int id=0;
    Color color;

    public ElectricalNode(int nodeNum) {
        id=nodeNum;
        color = new Color(
                (int)(128+Math.random()*127),
                (int)(128+Math.random()*127),
                (int)(128+Math.random()*127));
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