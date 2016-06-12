package iitc.triangulation.shapes;

import iitc.triangulation.aspect.Profile;

/**
 * @author epavlova
 * @version 07.06.2016
 */
public class KeysPriorities implements Profile {
    public static final KeysPriorities[] values = new KeysPriorities[] {
            new KeysPriorities(0, 0, 0, 0, 1, "ALL_KEYS"),
            new KeysPriorities(0, 1, 2, 3, 4, "DEFAULT"),
            new KeysPriorities(1, 0, 0, 0, 0, "SHORT"),
            new KeysPriorities(0, 0, 0, 0, 0, "RECKLESS")
    };
    public int muchFarm;
    public int maxFarm;
    public int needFarm;
    public int allKeys;
    public int haveKeys;
    public String name;

    public KeysPriorities(int muchFarm, int maxFarm, int needFarm, int allKeys, int haveKeys, String name) {
        this.muchFarm = muchFarm;
        this.maxFarm = maxFarm;
        this.needFarm = needFarm;
        this.allKeys = allKeys;
        this.haveKeys = haveKeys;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "KeysPriorities{" +
                "muchFarm=" + muchFarm +
                ", maxFarm=" + maxFarm +
                ", needFarm=" + needFarm +
                ", allKeys=" + allKeys +
                ", haveKeys=" + haveKeys +
                ", name='" + name + '\'' +
                '}';
    }
}
