package ElectronicsBackend;

import Editor.DraggableEditorComponent;
import Editor.EditorArea;
import Editor.Wire;
import ElectricalComponents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static ElectronicsBackend.ElectricalComponent.allComponents;
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
    List<ElectricalComponent> allExceptWireNode;

    HashSet<ElectricalNode> mnaNodes;
    private double[] outputValues;

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
        allExceptWireNode = ElectricalComponent.allComponents.stream()
                .filter(e -> !(e instanceof WireNode)).toList();

        simComponents  = new HashMap<>();
        for (ElectricalComponent component : allExceptWireNode) {
            Class<?> compType = findClassFromID(component.id);
            simComponents.putIfAbsent(compType, new ArrayList<>());
            ArrayList<ElectricalComponent> compList = simComponents.get(compType);
            compList.add(component);
        }

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
                if (ec instanceof Resistor) {
                    g2d.drawString( ec.getResistance()+"Ω",
                            dec.getX(), (int) (dec.getY()+.15*editor.scale));
                }
                if (ec instanceof VoltageSupply) {
                    g2d.drawString( ((VoltageSupply) ec).getVoltage()+"V",
                            dec.getX(), (int) (dec.getY()+.15*editor.scale));
                    g2d.drawString( ((VoltageSupply) ec).getCurrent()+"A",
                            dec.getX(), (int) (dec.getY()+.30* editor.scale));

                }
            }
        }
    }

    public void drawPaths(Graphics2D g2d) {
        if (outputValues == null) return;

        g2d.setFont(new Font("Arial", Font.BOLD, (int) (.14*editor.scale)));
        BasicStroke stippledStroke = new BasicStroke((float) (.07f*editor.scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stippledStroke);

        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        nodeList.sort(Comparator.comparingInt(n -> n.id)); // Sort by ID for consistent ordering

        for (int i = 0; i < nodeList.size(); i++) {
            ElectricalNode node = nodeList.get(i);
            g2d.setColor(node.color);

            // Display node voltage (first n values in output)
            g2d.drawString("Node " + node.id + ": " + round(outputValues[i], 3) + "V",
                    (int) editor.getWidth()-200, (int) 30+20*i);

            for (Wire w : node.wires) {
                Point2D.Double startWorld = w.startComponent.getConnectionPointsAsWorldPoints().get(w.startIndex);
                Point2D.Double endWorld = w.endComponent.getConnectionPointsAsWorldPoints().get(w.endIndex);
                Point startScreen = editor.worldToScreen(startWorld.x, startWorld.y);
                Point endScreen = editor.worldToScreen(endWorld.x, endWorld.y);
                g2d.drawLine(startScreen.x, startScreen.y, endScreen.x, endScreen.y);
            }
        }

        // Display voltage source currents (last m values in output)
        int n = nodeList.size();
        for (int k = 0; k < powerSupplies.size(); k++) {
            ElectricalComponent vs = powerSupplies.get(k);
            g2d.drawString(vs.shortenedId + " current: " + round(outputValues[n + k], 6) + "A",
                    (int) editor.getWidth()-200, (int) 30+20*(nodeList.size() + k + 1));
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

        // Remove ground node (set reference node to ground potential = 0)
        ElectricalNode groundNode = findGroundNode();
        if (groundNode != null) {
            mnaNodes.remove(groundNode);
            System.out.println("Removed ground node " + groundNode.id + " as reference");
        }

        // Reassign node IDs to be consecutive starting from 0
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        nodeList.sort(Comparator.comparingInt(n -> n.id));
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.get(i).id = i;
        }

        //n is number of nodes (excluding ground)
        //m is number of independent voltage sources
        int n = mnaNodes.size();
        int m = powerSupplies.size();

        System.out.println("Number of nodes (excluding ground): " + n);
        System.out.println("Number of voltage sources: " + m);

        if (n == 0 && m == 0) {
            System.out.println("No nodes or voltage sources to analyze");
            return;
        }

        double[][] matrixG = generateMatrixG(n, m);
        System.out.println("Matrix G:");
        showMatrix(matrixG);

        double[][] matrixB = generateMatrixB(n, m);
        System.out.println("\nMatrix B:");
        showMatrix(matrixB);

        double[][] matrixC = generateMatrixC(n, m);
        System.out.println("\nMatrix C:");
        showMatrix(matrixC);

        double[][] matrixD = generateMatrixD(n, m);
        System.out.println("\nMatrix D:");
        showMatrix(matrixD);

        double[][] matrixA = generateMatrixA(matrixG, matrixB, matrixC, matrixD);
        System.out.println("\nMatrix A:");
        showMatrix(matrixA);

        double[] b = generateArrayB();
        System.out.println("\nArray b:");
        System.out.println(Arrays.toString(b));

        if (matrixA.length > 0 && b.length > 0) {
            outputValues = gaussianElimination(matrixA, b);
            System.out.println("\nOutput Matrix (voltages then currents):");
            System.out.println(Arrays.toString(outputValues));
        }
    }

    private ElectricalNode findGroundNode() {
        for (ElectricalNode node : mnaNodes) {
            for (ElectricalComponent comp : node.getComponents()) {
                if (comp instanceof Ground) {
                    return node;
                }
            }
        }
        return null;
    }

    private double[][] generateMatrixA(double[][] matrixG, double[][] matrixB, double[][] matrixC, double[][] matrixD) {
        int n = matrixG.length;
        int m = matrixD.length;
        double[][] big = new double[n + m][n + m];

        // Copy G matrix to top-left
        for (int i = 0; i < n; i++)
            System.arraycopy(matrixG[i], 0, big[i], 0, n);

        // Copy B matrix to top-right
        for (int i = 0; i < n; i++)
            System.arraycopy(matrixB[i], 0, big[i], n, m);

        // Copy C matrix to bottom-left
        for (int i = 0; i < m; i++)
            System.arraycopy(matrixC[i], 0, big[n + i], 0, n);

        // Copy D matrix to bottom-right
        for (int i = 0; i < m; i++)
            System.arraycopy(matrixD[i], 0, big[n + i], n, m);

        return big;
    }

    private static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;

        // Create copies to avoid modifying originals
        double[][] Acopy = new double[n][n];
        double[] bcopy = new double[n];
        for (int i = 0; i < n; i++) {
            bcopy[i] = b[i];
            System.arraycopy(A[i], 0, Acopy[i], 0, n);
        }

        // Forward elimination with partial pivoting
        for (int i = 0; i < n; i++) {
            // Find pivot
            int max = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(Acopy[k][i]) > Math.abs(Acopy[max][i])) max = k;
            }

            // Swap rows
            double[] tempRow = Acopy[i];
            Acopy[i] = Acopy[max];
            Acopy[max] = tempRow;
            double tmp = bcopy[i];
            bcopy[i] = bcopy[max];
            bcopy[max] = tmp;

            // Check for singular matrix
            if (Math.abs(Acopy[i][i]) < 1e-10) {
                System.err.println("Matrix is singular or nearly singular at row " + i);
                return new double[n]; // Return zeros
            }

            // Eliminate column
            for (int k = i + 1; k < n; k++) {
                double factor = Acopy[k][i] / Acopy[i][i];
                for (int j = i; j < n; j++) {
                    Acopy[k][j] -= factor * Acopy[i][j];
                }
                bcopy[k] -= factor * bcopy[i];
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = bcopy[i];
            for (int j = i + 1; j < n; j++) {
                sum -= Acopy[i][j] * x[j];
            }
            x[i] = sum / Acopy[i][i];
        }
        return x;
    }

    public double[] generateArrayB() {
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        nodeList.sort(Comparator.comparingInt(n -> n.id)); // Ensure consistent ordering
        int n = nodeList.size();
        int m = powerSupplies.size();
        double[] b = new double[n + m];

        // For nodes: independent current sources contribute to RHS (typically 0 for passive circuits)
        // We assume no independent current sources, so first n entries remain 0

        // For voltage sources: set the voltage constraint
        for (int k = 0; k < m; k++) {
            if (powerSupplies.get(k) instanceof VoltageSupply) {
                VoltageSupply vs = (VoltageSupply) powerSupplies.get(k);
                b[n + k] = vs.getVoltage();
            }
            if (powerSupplies.get(k) instanceof PowerSupply) {
                PowerSupply vs = (PowerSupply) powerSupplies.get(k);
                b[n + k] = vs.getVoltage();
            }
        }

        return b;
    }

    public double[][] generateMatrixG(int n, int m) {
        double[][] G = new double[n][n];
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        nodeList.sort(Comparator.comparingInt(node -> node.id));

        for (int i = 0; i < n; i++) {
            ElectricalNode currentNode = nodeList.get(i);
            double selfConductance = 0.0;

            // Find all resistive components connected to this node
            for (ElectricalComponent comp : currentNode.getComponents()) {
                if (comp instanceof Resistor || (comp.getResistance() > 0)) {
                    double R = comp.getResistance();
                    if (R <= 0) continue;
                    double G_val = 1.0 / R;

                    // Find the other node this component connects to
                    ElectricalNode otherNode = findOtherConnectedNode(comp, currentNode);

                    if (otherNode != null && mnaNodes.contains(otherNode)) {
                        int j = getNodeIndex(otherNode, nodeList);
                        if (j >= 0 && j < n) {
                            G[i][j] -= G_val; // Off-diagonal: negative conductance
                        }
                    }
                    selfConductance += G_val; // Sum for diagonal
                }
            }
            G[i][i] = selfConductance; // Diagonal: sum of all conductances connected to node
        }

        return G;
    }

    private int getNodeIndex(ElectricalNode node, List<ElectricalNode> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).id == node.id) return i;
        }
        return -1;
    }

    private ElectricalNode findOtherConnectedNode(ElectricalComponent component, ElectricalNode currentNode) {
        // Find the other node this component is connected to
        for (ElectricalNode node : mnaNodes) {
            if (node != currentNode && node.getComponents().contains(component)) {
                return node;
            }
        }
        return null;
    }

    private ElectricalNode findOtherNode(ElectricalComponent eC, ElectricalNode currentNode) {
        // This method seems to be looking for connected components through children
        // This might need to be adapted based on your component connection model
        for (ElectricalComponent neighbor : eC.getChildren()) {
            for (ElectricalNode n : mnaNodes) {
                if (n.getComponents().contains(neighbor) && n != currentNode) {
                    return n;
                }
            }
        }
        return null;
    }

    public double[][] generateMatrixB(int n, int m) {
        double[][] B = new double[n][m];
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        nodeList.sort(Comparator.comparingInt(node -> node.id));

        for (int k = 0; k < m; k++) {
            ElectricalComponent voltageSource = powerSupplies.get(k);

            // Find the two nodes this voltage source connects
            ElectricalNode positiveNode = null;
            ElectricalNode negativeNode = null;

            int connectionCount = 0;
            for (ElectricalNode node : nodeList) {
                if (node.getComponents().contains(voltageSource)) {
                    if (connectionCount == 0) {
                        positiveNode = node; // Assume first connection is positive
                        connectionCount++;
                    } else if (connectionCount == 1) {
                        negativeNode = node; // Second connection is negative
                        break;
                    }
                }
            }

            // Set the B matrix entries
            if (positiveNode != null) {
                int posIndex = getNodeIndex(positiveNode, nodeList);
                if (posIndex >= 0) B[posIndex][k] = +1;
            }
            if (negativeNode != null) {
                int negIndex = getNodeIndex(negativeNode, nodeList);
                if (negIndex >= 0) B[negIndex][k] = -1;
            }
        }

        return B;
    }

    public double[][] generateMatrixC(int n, int m) {
        double[][] C = new double[m][n];
        double[][] B = generateMatrixB(n, m);

        // C is the transpose of B
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                C[j][i] = B[i][j];
            }
        }

        return C;
    }

    public double[][] generateMatrixD(int n, int m) {
        // D matrix is typically zero for circuits with only independent voltage sources
        double[][] D = new double[m][m];
        return D; // All zeros
    }

    private ElectricalNode findNodeForComponentConnection(ElectricalComponent eC, int terminalIndex) {
        int count = 0;
        for (ElectricalNode n : mnaNodes) {
            if (n.getComponents().contains(eC)) {
                if (count == terminalIndex) return n;
                count++;
            }
        }
        return null;
    }

    public HashSet<ElectricalNode> generateMNANodes() {
        record ConnectionKey(ElectricalComponent comp, int index) {}

        Map<ConnectionKey, Set<ConnectionKey>> adjacency = new HashMap<>();
        Map<ConnectionKey, Set<Wire>> connectionToWires = new HashMap<>();

        // Build adjacency graph from wires
        for (Wire w : editor.wires) {
            ConnectionKey start = new ConnectionKey(w.getStartComponent(), w.startIndex);
            ConnectionKey end   = new ConnectionKey(w.getEndComponent(), w.endIndex);

            adjacency.computeIfAbsent(start, k -> new HashSet<>()).add(end);
            adjacency.computeIfAbsent(end, k -> new HashSet<>()).add(start);

            connectionToWires.computeIfAbsent(start, k -> new HashSet<>()).add(w);
            connectionToWires.computeIfAbsent(end, k -> new HashSet<>()).add(w);
        }

        // Find connected components using DFS
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

        // Create ElectricalNode objects
        HashSet<ElectricalNode> nodes = new HashSet<>();
        int nodeNum = 0;
        for (Set<ConnectionKey> group : connectionGroups) {
            ElectricalNode node = new ElectricalNode(nodeNum);
            Set<Wire> groupWires = new HashSet<>();

            for (ConnectionKey ck : group) {
                node.addChild(ck.comp());
                groupWires.addAll(connectionToWires.getOrDefault(ck, Set.of()));
            }
            for (Wire w : groupWires) {
                node.addWire(w);
            }

            nodes.add(node);
            nodeNum++;
        }
        return nodes;
    }
}