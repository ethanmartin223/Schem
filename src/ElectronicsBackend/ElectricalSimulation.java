package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;
import ElectricalComponents.Ground;
import ElectricalComponents.PowerSupply;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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
                g2d.drawString( ec.shortenedId+(i+1),
                        dec.getX(), dec.getY());

            }
        }
    }

    public static double[][] multiply(double[][] a, double[][] b){

        double[][] outputMatrix =  new double[a.length][b[0].length];
        for (int y=0; y<a.length; y++){
            for (int x=0; x<a[y].length; x++){
                outputMatrix[y][x] = a[y][x] * b[x][y];
            }
        }
        return outputMatrix;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public static void showMatrix(double[][] a) {
        int maxLengths[] = new int[a[0].length];;
        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[y].length; x++) {
                maxLengths[x] = Math.max(maxLengths[x], (""+round(a[y][x], 2)).length());
            }
        }
        for (int y = 0; y < a.length; y++) {
            if  (y == 0)
                System.out.print("[[");
            else
                System.out.print(" [");

            for (int x = 0; x < a[y].length; x++) {
                String output = ""+round(a[y][x], 2);
                while  (output.length() < maxLengths[x]) {
                    output =" "+output;
                }
                System.out.print(output);
                if (x != (a[y].length-1))
                    System.out.print(", ");
                else if (y!=a.length-1)
                    System.out.print("], ");
                else
                    System.out.print("]]");
            }
            System.out.println();
        }
    }

    public double dotProduct(double[] a, double[] b) {
        assert a.length == b.length;
        double[] outArray = new double[a.length];
        for  (int i = 0; i < a.length; i++) {
            outArray[i] = a[i] * b[i];
        }
        return Arrays.stream(outArray).sum();
    }

    public static void main(String[] args) {

        double[][] a = new double[][]
            {{7,8},
             {9,10},
             {11,12}};

        double[][] b = new double[][]
           {{1,2,3},
            {4,5,6}};

        showMatrix(a);
        System.out.println();
        showMatrix(b);

        System.out.println();
        System.out.println();


        double[][] c = multiply(a,b);
        showMatrix(c);
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