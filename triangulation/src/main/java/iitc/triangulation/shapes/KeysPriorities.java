package iitc.triangulation.shapes;

import iitc.triangulation.aspect.Profile;

/**
 * @author epavlova
 * @version 07.06.2016
 */
public interface KeysPriorities extends Profile {
    KeysPriorities[] values = new KeysPriorities[]{
            new FarmedPriorities(0, 0, 0, 0, 1, "ALL_KEYS"),
            new FarmedPriorities(0, 1, 2, 3, 4, "DEFAULT"),
            new FarmedPriorities(1, 0, 0, 0, 0, "SHORT"),
            new FarmedPriorities(0, 0, 0, 0, 0, "RECKLESS"),
            new FixedPriorities(3, "3-KEYS"),
            new FixedPriorities(4, "4-KEYS"),
            new FixedPriorities(5, "5-KEYS"),
            new FixedPriorities(6, "6-KEYS"),
            new FixedPriorities(7, "7-KEYS"),
            new FixedPriorities(8, "8-KEYS"),
            new FixedPriorities(9, "9-KEYS"),
            new FixedPriorities(10, "10-KEYS"),
            new FixedPriorities(13, "13-KEYS")
    };

    int weight(int required, int available, int maxKeys);
}

