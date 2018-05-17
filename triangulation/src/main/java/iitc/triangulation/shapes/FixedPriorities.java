package iitc.triangulation.shapes;

public class FixedPriorities implements KeysPriorities {
    public int max;
    public String name;

    public FixedPriorities(int max, String name) {
        this.max = max;
        this.name = name;
    }

    public int weight(int required, int available, int maxKeys) {
        if (required > max) return 0;
        if (required == max) return 0;
        return 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FixedPriorities{" +
                "max=" + max +
                ", name='" + name + '\'' +
                '}';
    }
}
