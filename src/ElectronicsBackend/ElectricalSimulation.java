package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;
import ElectricalComponents.Ground;
import ElectricalComponents.PowerSupply;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.swing.*;

import static ElectronicsBackend.ElectricalComponent.findClassFromID;

public class ElectricalSimulation{

//https://cheever.domains.swarthmore.edu/Ref/mna/MNA2.html
//https://en.wikipedia.org/wiki/Kirchhoff%27s_circuit_laws

    EditorArea editor;
    HashMap<Class<?>, ArrayList<ElectricalComponent>> simComponents;

    public ElectricalSimulation(EditorArea editorArea) {
        editor = editorArea;

        List<ElectricalComponent> powerSupplies = ElectricalComponent.allComponents.stream()
                .filter(e -> e instanceof PowerSupply).toList();
        List<ElectricalComponent> grounds = ElectricalComponent.allComponents.stream()
                .filter(e -> e instanceof Ground).toList();
        List<ElectricalComponent> otherComponents = ElectricalComponent.allComponents.stream()
                .filter(e -> !(e instanceof Ground) && !(e instanceof PowerSupply)).toList();
        simComponents  = new HashMap<>();
        for (ElectricalComponent component : otherComponents) {
            Class<?> compType = findClassFromID(component.id);
            simComponents.putIfAbsent(compType, new ArrayList<>());
            ArrayList<ElectricalComponent> compList = simComponents.get(compType);
            compList.add(component);
        }

        System.out.println("Power Supplies:");
        System.out.println("\t"+powerSupplies);
        System.out.println("Grounds:");
        System.out.println("\t"+grounds);
        System.out.println("Components:");
        System.out.println("\t"+otherComponents);
    }

    public void draw(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, (int) (.14*editor.scale)));
        g2d.setColor(Color.BLUE);
        for (Class<?> t : simComponents.keySet()) {
            ArrayList<ElectricalComponent> currentComponentList = simComponents.get(t);
            for (int i =0; i<currentComponentList.size(); i++) {
                ElectricalComponent ec = currentComponentList.get(i);
                DraggableEditorComponent dec = ec.draggableEditorComponent;
                g2d.drawString( ec.shortenedId+ec.idNum,
                        dec.getX(), dec.getY());

            }
        }
    }

    public static class Node {
        List<Node> inputNodes;
        List<Node> outputNodes;

        double current = 0.0;

        public Node(double current) {
            inputNodes = new ArrayList<>();
            outputNodes = new ArrayList<>();

            this.current = current;
        }

        public void addChild(Node n) {
            outputNodes.add(n);
        }

        public void addParent(Node n) {
            inputNodes.add(n);
        }

        public double getCurrent() {
            return current;
        }


        public double calculateKirchoffCurrentLaw() {
            Function<List<Node>, Double> f = e ->
                    e.stream().mapToDouble(Node::getCurrent).sum();

            double inputSum =  f.apply(inputNodes);
            double outputSum = f.apply(outputNodes);
            return 0d;

        }

    }


}