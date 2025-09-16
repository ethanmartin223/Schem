package ElectronicsBackend;

import Editor.EditorArea;
import ElectricalComponents.Ground;
import ElectricalComponents.PowerSupply;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.swing.*;

public class ElectricalSimulation{

//https://cheever.domains.swarthmore.edu/Ref/mna/MNA2.html
//https://en.wikipedia.org/wiki/Kirchhoff%27s_circuit_laws

    EditorArea editor;

    public ElectricalSimulation(EditorArea editorArea) {
        editor = editorArea;

        Stream<ElectricalComponent> powerSupplies = ElectricalComponent.allComponents.stream().filter(e -> e instanceof PowerSupply);
        Stream<ElectricalComponent> grounds = ElectricalComponent.allComponents.stream().filter(e -> e instanceof Ground);

        System.out.println(powerSupplies);
        System.out.println(grounds);
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