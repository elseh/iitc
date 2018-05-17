package iitc.triangulation.shapes;

public class FarmedPriorities implements KeysPriorities {
    public int muchFarm;
    public int maxFarm;
    public int needFarm;
    public int allKeys;
    public int haveKeys;
    public String name;

    public FarmedPriorities(int muchFarm, int maxFarm, int needFarm, int allKeys, int haveKeys, String name) {
        this.muchFarm = muchFarm;
        this.maxFarm = maxFarm;
        this.needFarm = needFarm;
        this.allKeys = allKeys;
        this.haveKeys = haveKeys;
        this.name = name;
    }

    public int weight(int required, int available, int maxKeys) {
        int difference = required - available;
        if (difference > maxKeys) return muchFarm;
        if (difference == maxKeys) return maxFarm;
        if (difference > 0) return needFarm;
        if (difference == 0) return allKeys;
        if (difference < 0) return haveKeys;

        return available - required;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FarmedPriorities{" +
                "muchFarm=" + muchFarm +
                ", maxFarm=" + maxFarm +
                ", needFarm=" + needFarm +
                ", allKeys=" + allKeys +
                ", haveKeys=" + haveKeys +
                ", name='" + name + '\'' +
                '}';
    }
}
