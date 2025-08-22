package ElectricalLogic;

import java.util.ArrayList;

public class Component {
    //static
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
        int highestPriority = Integer.MIN_VALUE;
        Component currentHighest = null;
        for (int i=0; i<children.size(); i++) {
            if (currentHighest == null || (children.get(i).priority>highestPriority) && !visited) {
                highestPriority = children.get(i).priority;
                currentHighest = children.get(i);
                visited = true;
            }
        }
        return currentHighest;
    }

    public void addConnected(Component comp) {
        children.add(comp);
    }

    public void setVisited(boolean hasBeenVisited) {
        this.visited = hasBeenVisited;
    }

    public static void transverse(Component start) {
        ArrayList<Component> queue = new ArrayList<>();
        while (start.getNextComponent()!=null) {
            Component comp = start.getNextComponent();
            queue.add(comp);
            System.out.println("visited: "+comp.id);
        }
//
//        for (Component c : queue) {
//            transverse(c);
//        }
    }



    public static void main(String[] args) {

        // Component Structure
        //      a -- b
        //      |    |
        //      c -- d
        //

        Component a = new Component(0, "A");
        Component b = new Component(0, "B");
        Component c = new Component(0, "C");
        Component d = new Component(0, "D");

        a.addConnected(b);
        a.addConnected(c);
        b.addConnected(a);
        b.addConnected(d);
        c.addConnected(d);
        c.addConnected(a);
        d.addConnected(c);
        d.addConnected(b);

        transverse(a);

    }

}

