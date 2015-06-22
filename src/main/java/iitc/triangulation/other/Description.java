package iitc.triangulation.other;

import iitc.triangulation.Point;
import sun.security.krb5.internal.crypto.Des;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Sigrlinn on 15.06.2015.
 */
public class Description {
    private Map<Point, Integer> linkAmount = new HashMap<>();
    private int skipAmount = 0;
    private Set<Description> sumOf = new HashSet<>();

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

    public Description insert(Description d) {
        Description r = sum(this, d);
        r.sumOf = new HashSet<>(sumOf);
        r.sumOf.add(d);
        return r;
    }

    public static Description sum(Description a, Description b) {
        Description description = new Description();
        description.linkAmount = Stream.of(a, b)
                .flatMap(x -> x.linkAmount.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Description::sum));

        description.skipAmount = a.skipAmount + b.skipAmount;
        return description;
    }

    public static Description reverse(Description d) {
        Description description = new Description();
        d.linkAmount.forEach((k, v) -> description.linkAmount.put(k, 8-v));
        return description;
    }

    private static int sum(Integer a, Integer b) {
        a = a==null? 0 : a;
        b = b==null? 0 : b;
        return a+b;
    }

    public static Description min(Description a, Description b) {
        return a.skipAmount < b.skipAmount ? a : b;
    }

    public static Description reduce(Description a, Set<Point> pointSet) {
        Description d = new Description();
        pointSet.stream().forEach(p -> d.linkAmount.put(p, a.linkAmount.get(p)));
        d.skipAmount = a.skipAmount;
        d.sumOf = a.sumOf;
        return d;
    }

    public Set<Description> getSumOf() {
        return sumOf;
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


