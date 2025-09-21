package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;
import Editor.Wire;
import ElectricalComponents.Ground;
import ElectricalComponents.PowerSupply;
import ElectricalComponents.VoltageSupply;
import ElectricalComponents.WireNode;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static ElectronicsBackend.ElectricalComponent.findClassFromID;

public class ElectricalSimulation{

//https://cheever.domains.swarthmore.edu/Ref/mna/MNA2.html
//https://en.wikipedia.org/wiki/Kirchhoff%27s_circuit_laws

    EditorArea editor;
    public HashMap<Class<?>, ArrayList<ElectricalComponent>> simComponents;
    List<ElectricalComponent> otherComponents;
    List<ElectricalComponent> powerSupplies;
    List<ElectricalComponent> grounds;
    List<ElectricalComponent> wireNodes;

    HashSet<ElectricalNode> mnaNodes;
    public ElectricalSimulation(EditorArea editorArea) {
        editor = editorArea;

        powerSupplies = ElectricalComponent.allComponents.stream()
                .filter(e -> e instanceof PowerSupply || e instanceof VoltageSupply).toList();
        grounds = ElectricalComponent.allComponents.stream()
                .filter(e -> e instanceof Ground).toList();
        wireNodes = ElectricalComponent.allComponents.stream()
                .filter(e -> e instanceof WireNode).toList();
        otherComponents = ElectricalComponent.allComponents.stream()
                .filter(e -> !(e instanceof Ground) && !(e instanceof PowerSupply)
                        && !(e instanceof VoltageSupply)
                        && !(e instanceof WireNode)).toList();
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

        runMNA();
    }

    public void drawIdentifiers(Graphics2D g2d) {
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


    public void drawPaths(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, (int) (.14*editor.scale)));
        BasicStroke stippledStroke = new BasicStroke((float) (.07f*editor.scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stippledStroke);
        for (ElectricalNode node : mnaNodes) {
            g2d.setColor(node.color);
            double avgX = 0;
            double avgY = 0;
            for (ElectricalComponent eC : node.components) {
                avgX += eC.getDraggableEditorComponent().getX()+
                        (double) eC.getDraggableEditorComponent().getWidth() /2;
                avgY += eC.getDraggableEditorComponent().getY()+
                        (double) eC.getDraggableEditorComponent().getHeight() /2;
            }
            avgX /= node.components.size();
            avgY /= node.components.size();
            LineMetrics lm = g2d.getFont().getLineMetrics("Node("+node.id+")", g2d.getFontRenderContext());
            float width = g2d.getFontMetrics().stringWidth("Node("+node.id+")");
            g2d.drawString("Node("+node.id+")", (int) avgX- width/4, (int) avgY-lm.getHeight());
            for (Wire w : node.wires) {
                Point2D.Double startWorld =w.startComponent.getConnectionPointsAsWorldPoints().get(w.startIndex);
                Point2D.Double endWorld = w.endComponent.getConnectionPointsAsWorldPoints().get(w.endIndex);
                Point startScreen = editor.worldToScreen(startWorld.x, startWorld.y);
                Point endScreen = editor.worldToScreen(endWorld.x, endWorld.y);
                g2d.drawLine(startScreen.x, startScreen.y, endScreen.x, endScreen.y);
            }
        }
    }

    public static double[] grabCol(double[][] a, int x) {
        double[] output =  new double[a.length];
        for (int y=0; y<a.length; y++){
            output[y] = a[y][x];
        }
        return output;
    }

    public static double[][] multiply(double[][] a, double[][] b){

        double[][] outputMatrix =  new double[a.length][b[0].length];
        for (int y=0; y<a.length; y++){
            for (int x=0; x<b[0].length; x++){
                outputMatrix[y][x] = dotProduct(grabCol(b, x),a[y]);
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

    public static double dotProduct(double[] a, double[] b) {
        assert a.length == b.length;
        double[] outArray = new double[a.length];
        for  (int i = 0; i < a.length; i++) {
            outArray[i] = a[i] * b[i];
        }
        return Arrays.stream(outArray).sum();
    }



    // ----------------------- mna algorithm --------------------------- //
    public void runMNA() {
        mnaNodes = generateMNANodes();

        //n is number of nodes
        //m is number of independent voltage sources
        int n = mnaNodes.size();
        int m = powerSupplies.size();

        double[][] matrixG = generateMatrixG(n,m);
        System.out.println("Matrix G:");
        showMatrix(matrixG);

        double[][] matrixB = generateMatrixB(n,m);
        System.out.println("\nMatrix B:");
        showMatrix(matrixB);

        double[][] matrixC = generateMatrixC(n,m);
        System.out.println("\nMatrix C:");
        showMatrix(matrixC);

        double[][] matrixD = generateMatrixD(n,m);
        System.out.println("\nMatrix D:");
        showMatrix(matrixD);

    }

    public double[][] generateMatrixG(int n, int m) {
        double[][] G = new double[n][n];

        for (int i = 0; i < n; i++) {
            ElectricalNode currentNode = null;
            for (ElectricalNode nd : mnaNodes) {
                if (nd.id == i) {
                    currentNode = nd;
                    break;
                }
            }
            if (currentNode == null) continue;

            double diagConductance = 0.0;

            for (ElectricalComponent eC : currentNode.getComponents()) {
                double R = eC.getResistance();
                if (R <= 0) continue; // skip wires/open circuits

                double Gval = 1.0 / R;
                ElectricalNode otherNode = findOtherNode(eC, currentNode);

                if (otherNode != null) {
                    int j = otherNode.id;
                    if (j >= 0 && j < n) {
                        // Off-diagonal entry: subtract conductance
                        G[i][j] -= Gval;
                        G[j][i] -= Gval; // keep symmetric
                    }
                }

                // Diagonal always gets the conductance contribution
                diagConductance += Gval;
            }

            G[i][i] += diagConductance;
        }

        return G;
    }

    private ElectricalNode findOtherNode(ElectricalComponent eC, ElectricalNode currentNode) {
        for (ElectricalComponent neighbor : eC.getChildren()) {
            // Check which node this neighbor belongs to
            for (ElectricalNode n : mnaNodes) {
                if (n.getComponents().contains(neighbor) && n != currentNode) {
                    return n;
                }
            }
        }
        return null;
    }


    public double[][] generateMatrixB(int n, int m) {
        double[][] output = new double[n][m];
        return output;
    }

    public double[][] generateMatrixC(int n, int m) {
        double[][] output = new double[m][n];
        return output;
    }

    public double[][] generateMatrixD(int n, int m) {
        double[][] output = new double[m][m];
        return output;
    }

    public HashSet<ElectricalNode> generateMNANodes() {
        record ConnectionKey(ElectricalComponent comp, int index) {}

        Map<ConnectionKey, Set<ConnectionKey>> adjacency = new HashMap<>();
        Map<ConnectionKey, Set<Wire>> connectionToWires = new HashMap<>();

        for (Wire w : editor.wires) {
            ConnectionKey start = new ConnectionKey(w.getStartComponent(), w.startIndex);
            ConnectionKey end   = new ConnectionKey(w.getEndComponent(), w.endIndex);

            adjacency.computeIfAbsent(start, k -> new HashSet<>()).add(end);
            adjacency.computeIfAbsent(end, k -> new HashSet<>()).add(start);

            connectionToWires.computeIfAbsent(start, k -> new HashSet<>()).add(w);
            connectionToWires.computeIfAbsent(end, k -> new HashSet<>()).add(w);
        }

        Set<ConnectionKey> visited = new HashSet<>();
        List<Set<ConnectionKey>> connectionGroups = new ArrayList<>();

        for (ConnectionKey key : adjacency.keySet()) {
            if (visited.contains(key)) continue;

            Set<ConnectionKey> group = new HashSet<>();
            Deque<ConnectionKey> stack = new ArrayDeque<>();
            stack.push(key);

            while (!stack.isEmpty()) {
                ConnectionKey current = stack.pop();
                if (!visited.add(current)) continue;
                group.add(current);

                for (ConnectionKey neighbor : adjacency.getOrDefault(current, Set.of())) {
                    if (!visited.contains(neighbor)) stack.push(neighbor);
                }
            }
            connectionGroups.add(group);
        }

        HashSet<ElectricalNode> nodes = new HashSet<>();
        int nodeNum = 0;
        for (Set<ConnectionKey> group : connectionGroups) {
            ElectricalNode node = new ElectricalNode(nodeNum);
            Set<Wire> groupWires = new HashSet<>();

            for (ConnectionKey ck : group) {
                node.addChild(ck.comp());
                groupWires.addAll(connectionToWires.getOrDefault(ck, Set.of()));
            }

            // add all wires that touch this node
            for (Wire w : groupWires) {
                node.addWire(w);
            }

            nodes.add(node);
            nodeNum+=1;
        }
        return nodes;
    }
}