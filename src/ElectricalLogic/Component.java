package ElectricalLogic;

import java.util.*;

class Component {

    // static
    public static boolean debugMode = false;
    public static ArrayList<Component> allComponents = new ArrayList<>();

    // fields
    private int resistance;
    private ArrayList<Component> children;
    private String id;

    // constructor
    public Component(int resistance, String id) {
        this.resistance = resistance;
        this.children = new ArrayList<>();
        this.id = id;
        allComponents.add(this);
    }

    public void connect(Component other) {
        if (other == this) return;
        this.children.add(other);
        other.children.add(this);
    }

    public int getResistance() {
        return resistance;
    }

    public ArrayList<Component> getChildren() {
        return children;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Component(" + id + ")";
    }


    public static List<Component> findPathOfLeastResistance(Component start, Component end) {
        Map<Component, Integer> dist = new HashMap<>();
        Map<Component, Component> prev = new HashMap<>();
        PriorityQueue<Component> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (Component c : allComponents) {
            dist.put(c, Integer.MAX_VALUE);
            prev.put(c, null);
        }
        dist.put(start, 0);
        pq.add(start);
        while (!pq.isEmpty()) {
            Component u = pq.poll();
            if (u == end) break;
            for (Component v : u.getChildren()) {
                int alt = dist.get(u) + v.getResistance();
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        List<Component> path = new ArrayList<>();
        Component step = end;
        if (prev.get(step) != null || step == start) {
            while (step != null) {
                path.add(0, step);
                step = prev.get(step);
            }
        }
        return path;
    }

    public static List<List<Component>> findAllPaths(Component start, Component end) {
        List<List<Component>> allPaths = new ArrayList<>();
        Queue<List<Component>> queue = new LinkedList<>();

        List<Component> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.add(initialPath);

        while (!queue.isEmpty()) {
            List<Component> path = queue.poll();
            Component last = path.get(path.size() - 1);

            if (last.equals(end)) {
                allPaths.add(new ArrayList<>(path));
            } else {
                for (Component neighbor : last.getChildren()) {
                    if (!path.contains(neighbor)) {
                        List<Component> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        queue.add(newPath);
                    }
                }
            }
        }
        return allPaths;
    }


    public static char[] compListToCharList(List<Component> compList) {
        char[] output = new char[compList.size()];
        for (int i = 0; i < compList.size(); i++) {
            output[i] = compList.get(i).id.charAt(0);
        }
        return output;
    }

    public static void main(String[] args) {

        // Component Structure
        //
        //     a -- b -- c -- d
        //     |    |         |
        //     |    |         |
        //     g -- h -- i -- j
        //

        Component a = new Component(0, "A");
        Component b = new Component(1, "B");
        Component c = new Component(1, "C");
        Component d = new Component(1, "D");
        Component g = new Component(1, "G");
        Component h = new Component(1, "H");
        Component i = new Component(1, "I");
        Component j = new Component(1, "J");

        a.connect(b);
        b.connect(c);
        c.connect(d);
        a.connect(g);
        g.connect(h);
        h.connect(i);
        i.connect(j);
        b.connect(h);
        d.connect(j);


//        List<Component> path = findPath(a, w);
//        System.out.println(Arrays.toString(compListToCharList(path)));

        List<List<Component>> allPaths = findAllPaths(a, j);
        for (List<Component> path : allPaths) {
            System.out.println(Arrays.toString(compListToCharList(path)));
        }

    }
}
