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


    public static List<Component> findPath(Component start, Component end) {
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
        //        a ---- b ---- e ---- i ---- m
        //        |      |      |      |      |
        //        c ---- r ---- g ---- j ---- n
        //        |      |      |      |      |
        //        t ---- f ---- h ---- k ---- o
        //        |             |      |      |
        //        x ---- y ---- z ---- l ---- p
        //               |             |
        //               q ------------w
        //        s


        Component a = new Component(0, "A");
        Component b = new Component(1, "B");
        Component c = new Component(2, "C");
        Component e = new Component(2, "E");
        Component f = new Component(1, "F");
        Component g = new Component(2, "G");
        Component h = new Component(1, "H");
        Component i = new Component(0, "I");
        Component j = new Component(2, "J");
        Component k = new Component(1, "K");
        Component l = new Component(3, "L");
        Component m = new Component(0, "M");
        Component n = new Component(1, "N");
        Component o = new Component(2, "O");
        Component p = new Component(0, "P");
        Component r = new Component(1, "R");
        Component t = new Component(2, "T");
        Component x = new Component(10, "X");
        Component y = new Component(0, "Y");
        Component z = new Component(2, "Z");
        Component q = new Component(3, "Q");
        Component w = new Component(1, "W");
        Component s = new Component(1, "S");

        a.connect(b); a.connect(c);
        b.connect(e); b.connect(r);
        c.connect(r); c.connect(t);

        e.connect(i); e.connect(g);
        f.connect(r); f.connect(t); f.connect(h);

        g.connect(r); g.connect(j); g.connect(h);
        h.connect(g); h.connect(f); h.connect(k);

        i.connect(m); i.connect(j);
        j.connect(n); j.connect(k);
        k.connect(o); k.connect(l);

        l.connect(p); l.connect(w);
        m.connect(n);
        n.connect(o);
        o.connect(p);

        t.connect(x);
        x.connect(y);
        y.connect(z); y.connect(q);
        z.connect(h); z.connect(l);
        q.connect(w);


        List<Component> path = findPath(a, w);
        System.out.println(Arrays.toString(compListToCharList(path)));
    }
}
