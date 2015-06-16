package iitc.triangulation.other;

import iitc.triangulation.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 15.06.2015.
 */
public class Description {
    private Map<Point, Integer> linkAmount = new HashMap<>();
    private int skipAmount = 0;

    public static Description skipAll(Set<Point> pointSet, int amount) {
        Description description = new Description();
        description.skipAmount = amount;
        //System.out.println("a: " + amount);
        pointSet.stream().forEach(p -> description.linkAmount.put(p, 0));
        return description;
    }

    public static Description makeBase(Set<Point> pointSet) {
        Description description = new Description();
        pointSet.stream().forEach(p -> description.linkAmount.put(p, 1));
        return description;
    }

    public static Description makeEmptyBase(Set<Point> pointSet) {
        Description description = new Description();
        pointSet.stream().forEach(p -> description.linkAmount.put(p, 0));
        return description;
    }

    public static Description sum(Description a, Description b) {
        Description description = new Description();
        description.linkAmount = new HashMap<>(a.linkAmount);
        Set<Point> keys = new HashSet<>();
        keys.addAll(a.linkAmount.keySet());
        keys.addAll(b.linkAmount.keySet());

        for (Point p : keys) {
            Integer a1 = a.linkAmount.get(p);
            Integer a2 = b.linkAmount.get(p);
            description.linkAmount.put(p, Integer.sum(
                            (a1 != null) ? a1 : 0,
                            (a2 != null) ? a2 : 0)
            );
        }

        description.skipAmount = a.skipAmount + b.skipAmount;
        return description;
    }

    public static Description min(Description a, Description b) {
        return a.skipAmount < b.skipAmount ? a : b;
    }

    public static Description reduce(Description a, Set<Point> pointSet) {
        Description d = new Description();
        pointSet.stream().forEach(p -> d.linkAmount.put(p, a.linkAmount.get(p)));
        d.skipAmount = a.skipAmount;
        return d;
    }


    public Map<Point, Integer> getLinkAmount() {
        return linkAmount;
    }

    public int getSkipAmount() {
        return skipAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Description that = (Description) o;

        return !(linkAmount != null ? !linkAmount.equals(that.linkAmount) : that.linkAmount != null);

    }

    @Override
    public int hashCode() {
        return linkAmount != null ? linkAmount.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "[" + skipAmount + "=" + String.join(", ", linkAmount.values().stream().map(i -> i + "").collect(Collectors.joining(", ")) + "]");
    }
}


