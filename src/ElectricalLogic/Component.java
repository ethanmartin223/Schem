package ElectricalLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

class Component {

    //static
    public static boolean debugMode = false;
    public static ArrayList<Component> allComponents = new ArrayList<>();

    //private
    private int priority;
    private ArrayList<Component> children;
    private boolean visited;
    private String id;

    //const
    public Component(int priority, String id) {
        this.priority = priority;
        this.children = new ArrayList<>();
        this.visited = false;
        this.id = id;

        allComponents.add(this);
    }

    public Component getNextComponent() {
        int highestPriority = Integer.MAX_VALUE;
        Component currentHighest = null;
        for (int i=0; i<children.size(); i++) {
            if (debugMode) System.out.println("\t"+children.get(i).priority +"<"+ highestPriority+" && "+!children.get(i).visited);
            if (children.get(i).priority == highestPriority && !children.get(i).visited) {

                if (debugMode) System.out.println("[Starting path search for path1]");
                int path1 = totalTranversalResistance(transverse(children.get(i), true));

                if (debugMode) System.out.println("[Starting path search for path2]");
                int path2 = totalTranversalResistance(transverse(currentHighest, true));

                if (path1 < path2) currentHighest = children.get(i);
                else if (path1 > path2) currentHighest = currentHighest;
                else currentHighest = Math.random() > .5?children.get(i):currentHighest;
                highestPriority = currentHighest.priority;

            } else if (children.get(i).priority < highestPriority && !children.get(i).visited) {
                highestPriority = children.get(i).priority;
                currentHighest = children.get(i);
            }
        }
        if (debugMode) System.out.println("\t"+children);
        if (currentHighest!= null) currentHighest.visited = true;
        if (debugMode) System.out.println("\tcurrentHighest: "+currentHighest);
        return currentHighest;
    }

    public void addConnected(Component comp) {
        children.add(comp);
    }

    public void setVisited(boolean hasBeenVisited) {
        this.visited = hasBeenVisited;
    }

    public static void calcComponent(Component c) {
        //do calc here for priority
        if (debugMode) System.out.println("\n\n[visited node: "+c.id+"]");
    }

    @Override
    public String toString() {
        return "Component("+this.id+")";
    }



    public static ArrayList<Component> transverse(Component start) {
        return transverse(start, false);
    }

    private static ArrayList<Component> transverse(Component start, boolean readOnly) {
        Component comp = start;
        start.visited = true;
        ArrayList<Component> output = new ArrayList<>();

        HashMap<Component, Boolean> visitedData = new HashMap<>();
        if (readOnly) {
            if (debugMode) System.out.println("[internal node search]");
            for (Component c : allComponents) {
                visitedData.put(c, c.visited);
            }
        }

        while (comp!=null) {
            calcComponent(comp);
            output.add(comp);
            comp = comp.getNextComponent();
        }

        if (readOnly) {
            for (Component c : allComponents) {
                c.visited = visitedData.get(c);
            }
        }
        return output;
    }

    public int totalTranversalResistance(ArrayList<Component> path) {
        int total = 0;
        for (Component c : path) {
            total+=c.priority;
        }
        return total;
    }

    public static char[] compListToCharList(ArrayList<Component> compList) {
        char[] output = new char[compList.size()];
        for (int i =0; i<compList.size(); i++) {
            output[i] = compList.get(i).id.charAt(0);
        }
        return output;
    }



    public static void main(String[] args) {

        // Component Structure
        //      a -- b -- e
        //      |    |    |
        //      c -- d -- g
        //           |
        //           f

        Component a = new Component(0, "A");
        Component b = new Component(0, "B");
        Component c = new Component(0, "C");
        Component d = new Component(1, "D");
        Component e = new Component(0, "E");
        Component f = new Component(0, "F");
        Component g = new Component(0, "G");

        a.addConnected(b);
        a.addConnected(c);
        b.addConnected(a);
        b.addConnected(d);
        b.addConnected(e);
        c.addConnected(d);
        c.addConnected(a);
        d.addConnected(c);
        d.addConnected(b);
        d.addConnected(f);
        e.addConnected(b);
        e.addConnected(g);
        f.addConnected(d);
        d.addConnected(g);
        d.addConnected(f);
        g.addConnected(d);
        g.addConnected(e);

        HashSet<char[]> hs = new HashSet<>();
        for (int i=0; i<1000; i++) {
            boolean dontAdd= false;
            char[] toAdd = compListToCharList(transverse(a));
            for (char[] ex: hs) {
                if (ex.length!= toAdd.length) {
                    continue;
                }
                boolean exists = true;
                for (int x=0; x<ex.length; x++) {
                    if (toAdd[x] != ex[x]) exists = false;
                }
                if (exists) dontAdd = true;
            }
            if (!dontAdd) hs.add(toAdd);
            for (Component comp : allComponents) comp.visited = false;

        }

        for (char[] i : hs) {
            System.out.println(Arrays.toString(i));
        }
    }

}