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
        g2d.setFont(new Font("Arial", Font.BOLD, (int) (.14*editor.scale)));
        BasicStroke stippledStroke = new BasicStroke((float) (.07f*editor.scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stippledStroke);
        for (ElectricalNode node : mnaNodes) {
            g2d.setColor(node.color);

            g2d.drawString(round(outputValues[node.id],2)+" Amps", (int) editor.getWidth()-150, (int) 30+30* node.id);
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
        List<ElectricalNode> orderedNodeList = new ArrayList<>(mnaNodes);

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

        double[][] matrixA = generateMatrixA(matrixG,matrixB,matrixC,matrixD);
        System.out.println("\nMatrix A:");
        showMatrix(matrixA);

        double It = 0.002;
        double Vg = 5.0;
        double Vx = 2.0;
        double[] z = new double[n+m];

        double[] b = generateArrayB();
        outputValues  = gaussianElimination(matrixA, b);

        System.out.println("\nOutput Matrix:");
        System.out.println(Arrays.toString(outputValues));



    }


    private double[][] generateMatrixA(double[][] matrixG, double[][] matrixB, double[][] matrixC, double[][] matrixD) {
        int n = matrixG.length;
        int m = matrixD.length;
        double[][] big = new double[n + m][n + m];
        for (int i = 0; i < n; i++)
            System.arraycopy(matrixG[i], 0, big[i], 0, n);
        for (int i = 0; i < n; i++)
            System.arraycopy(matrixB[i], 0, big[i], n + 0, m);
        for (int i = 0; i < m; i++)
            System.arraycopy(matrixC[i], 0, big[n + i], 0, n);
        for (int i = 0; i < m; i++)
            System.arraycopy(matrixD[i], 0, big[n + i], n + 0, m);
        return big;
    }

    private static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;
        for (int i = 0; i < n; i++) {
            int max = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[max][i])) max = k;
            }
            double[] tempRow = A[i];
            A[i] = A[max];
            A[max] = tempRow;
            double tmp = b[i];
            b[i] = b[max];
            b[max] = tmp;
            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                for (int j = i; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
                b[k] -= factor * b[i];
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * x[j];
            }
            x[i] = sum / A[i][i];
        }
        return x;
    }

    public void printBVectorExplanation(double[] b) {
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes);
        int n = nodeList.size();
        int m = powerSupplies.size();

        System.out.println("=== MNA vector b explanation ===");
        for (int i = 0; i < n; i++) {
            System.out.println("b[" + i + "] (Node " + nodeList.get(i).id + "): Current contribution from connected voltage sources = " + b[i] + " A");
        }
        for (int k = 0; k < m; k++) {
            VoltageSupply vs = (VoltageSupply) powerSupplies.get(k);
            System.out.println("b[" + (n + k) + "] (Voltage source " + vs.shortenedId + "): Voltage value = " + b[n + k] + " V");
        }
        System.out.println("================================");
    }



    public double[] generateArrayB() {
        List<ElectricalNode> nodeList = new ArrayList<>(mnaNodes); // fixed ordering
        int n = nodeList.size();
        int m = powerSupplies.size();
        double[] b = new double[n + m];

        for (int k = 0; k < m; k++) {
            VoltageSupply vs = (VoltageSupply) powerSupplies.get(k);
            double current = vs.getCurrent();
            double voltage = vs.getVoltage();

            // Find the two nodes this voltage supply connects to
            ElectricalNode node1 = null;
            ElectricalNode node2 = null;
            for (ElectricalNode node : nodeList) {
                if (node.getComponents().contains(vs)) {
                    if (node1 == null) node1 = node;
                    else { node2 = node; break; } // second node found
                }
            }

            int index1 = node1 != null ? nodeList.indexOf(node1) : -1;
            int index2 = node2 != null ? nodeList.indexOf(node2) : -1;

            // Add current contribution
            if (index1 >= 0) b[index1] += current;
            if (index2 >= 0) b[index2] -= current;

            // Extra row for voltage source equation
            b[n + k] = voltage;
        }

        return b;
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
                if (R <= 0) continue;

                double Gval = 1.0 / R;
                ElectricalNode otherNode = findOtherNode(eC, currentNode);

                if (otherNode != null) {
                    int j = otherNode.id;
                    if (j >= 0 && j < n) {
                        G[i][j] -= Gval;
                        G[j][i] -= Gval;
                    }
                }
                diagConductance += Gval;
            }

            G[i][i] += diagConductance;
        }

        return G;
    }

    private ElectricalNode findOtherNode(ElectricalComponent eC, ElectricalNode currentNode) {
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
        int vIndex = 0;
        for (ElectricalComponent eC : ElectricalComponent.allComponents) {
            if (!(eC instanceof VoltageSupply || eC instanceof PowerSupply)) continue; // only voltage sources go here

            ElectricalNode node1 = findNodeForComponentConnection(eC, 0);
            ElectricalNode node2 = findNodeForComponentConnection(eC, 1);

            if (node1 != null) B[node1.id][vIndex] = +1;
            if (node2 != null) B[node2.id][vIndex] = -1;

            vIndex++;
        }

        return B;
    }

    public double[][] generateMatrixC(int n, int m) {
        double[][] C = new double[m][n];
        double[][] B = generateMatrixB(n, m);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                C[j][i] = B[i][j];
            }
        }

        return C;
    }

    public double[][] generateMatrixD(int n, int m) {
        double[][] D = new double[m][m];
        return D;
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
            for (Wire w : groupWires) {
                node.addWire(w);
            }

            nodes.add(node);
            nodeNum+=1;
        }
        return nodes;
    }
}