package iitc.triangulation.other;

import iitc.triangulation.Point;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Sigrlinn on 15.06.2015.
 */
public class Description {
    private Map<Point, Integer> linkAmount = new HashMap<>();
    private Set<Description> sumOf = new HashSet<>();

    public static Description skipAll(Set<Point> pointSet) {
        Description description = new Description();
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
        return description;
    }

    public static Description reverse(Description d) {
        Description description = new Description();
        d.linkAmount.forEach((k, v) -> description.linkAmount.put(k, k.getMaxLinks()-v));
        return description;
    }

    private static int sum(Integer a, Integer b) {
        a = a==null? 0 : a;
        b = b==null? 0 : b;
        return a+b;
    }

    public boolean checkSumInTheInnerPoint() {
        Set<Point> allPoints = getSumOf()
                .stream()
                .flatMap(s -> s.getLinkAmount().keySet().stream())
                .collect(Collectors.toSet());
        allPoints.removeAll(getLinkAmount().keySet());
        Optional<Point> first = allPoints.stream().findFirst();
        if (first.isPresent() && allPoints.size() == 1) {
            Point p = first.get();
            int sum = getSumOf().stream().mapToInt(i -> i.getLinkAmount().get(p)).sum();
            boolean b = sum <= p.getMaxLinks();
            if (!b) {
                System.out.println("fail in: " + this + " " + sum);
            }
            return b;
        }
        return true;
    }

    public int getSumInTheInnerPoint() {
        Set<Point> allPoints = getSumOf()
                .stream()
                .flatMap(s -> s.getLinkAmount().keySet().stream())
                .collect(Collectors.toSet());
        allPoints.removeAll(getLinkAmount().keySet());
        Optional<Point> first = allPoints.stream().findFirst();
        if (first.isPresent() && allPoints.size() == 1) {
            Point p = first.get();
            return getSumOf().stream().mapToInt(i -> i.getLinkAmount().get(p)).sum();
        }
        return 0;
    }



    public static Description min(Description a, Description b) {
        return a.getSumInTheInnerPoint() < b.getSumInTheInnerPoint() ? a : b;
    }

    public static Description reduce(Description a, Set<Point> pointSet) {
        Description d = new Description();
        pointSet.stream().forEach(p -> d.linkAmount.put(p, a.linkAmount.get(p)));
        d.sumOf = a.sumOf;
        return d;
    }

    public Set<Description> getSumOf() {
        return sumOf;
    }

    public Map<Point, Integer> getLinkAmount() {
        return linkAmount;
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
        return "[" + String.join(", ", linkAmount.values().stream().map(i -> i + "").collect(Collectors.joining(", ")) + "]");
    }

    public void test() {
        ArrayList<Point> list = new ArrayList<>(linkAmount.keySet());
        linkAmount.put(list.get(0), 7);
        linkAmount.put(list.get(1), 8);
        linkAmount.put(list.get(2), 2);
    }

    public Description testAdd(Map<Point, Set<Point>> pointSetMap) {
        Description description = new Description();
        linkAmount.forEach((k, v) -> description.linkAmount.put(k, k.getMaxLinks()));
        pointSetMap.forEach((k, v) -> description.linkAmount.put(k, v.size() + linkAmount.get(k)));
        return description;

    }
}


