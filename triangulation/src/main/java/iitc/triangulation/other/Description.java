package iitc.triangulation.other;

import iitc.triangulation.Point;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 15.06.2015.
 */
public class Description {
    private Map<Point, Integer> linkAmount = new HashMap<>();
    private Set<Description> sumOf = new HashSet<>();

    private Description() {
    }

    static Collector<Description, ?, Map<Map<Point, Integer>, Description>> TO_MAP =
            Collectors.toMap(Description::getLinkAmount, Function.identity(), Description::min);

    void makeFinal() {
        counted = false;
        getSumInTheInnerPoint();
    }

    static Description skipAll(Set<Point> pointSet) {
        Description description = new Description();
        pointSet.forEach(p -> description.linkAmount.put(p, 0));
        description.makeFinal();
        return description;
    }

    static Description makeBase(Set<Point> pointSet) {
        Description description = new Description();
        pointSet.forEach(p -> description.linkAmount.put(p, 1));
        description.makeFinal();
        return description;
    }

    static Description makeEmptyBase(Set<Point> pointSet) {
        Description description = new Description();
        pointSet.forEach(p -> description.linkAmount.put(p, 0));
        return description;
    }

    Description insert(Description d) {
        Description description = new Description();
          linkAmount.forEach((key, value) -> description.linkAmount.merge(key, value, (v1, v2) -> v1 + v2));
        d.linkAmount.forEach((key, value) -> description.linkAmount.merge(key, value, (v1, v2) -> v1 + v2));

        description.sumOf.addAll(sumOf);
        description.sumOf.add(d);
        description.makeFinal();
        return description;
    }

    static Description reverse(Description d) {
        Description description = new Description();
        d.linkAmount.forEach((k, v) -> description.linkAmount.put(k, k.getMaxLinks()-v));
        description.makeFinal();
        return description;
    }

    boolean checkDescriptionGoodness() {
        if (!counted)
            getSumInTheInnerPoint();
        return isGoodDescription;
    }

    private int sumInInner = 0;
    private boolean counted = false;
    private boolean isGoodDescription;
    private int getSumInTheInnerPoint() {
        if (counted) return sumInInner;
        Map<Point, Integer> sum = new HashMap<>();
        getSumOf().forEach(description -> description.getLinkAmount()
                .forEach((key, value) -> {
                    if (!linkAmount.containsKey(key)) sum.merge(key, value, (value1, value2) -> value1+value2);
                })
        );
        isGoodDescription = getLinkAmount().entrySet().stream().noneMatch(entry ->
                entry.getKey().getMaxLinks() < entry.getValue()
        );
        if (sum.size() == 1) {
            for (Map.Entry<Point, Integer> entry : sum.entrySet()) {
                Point innerPoint = entry.getKey();
                sumInInner = entry.getValue();
                isGoodDescription &= innerPoint.getMaxLinks() >= sumInInner;
            }
        }
        counted = true;
        return sumInInner;
    }

    private static Description min(Description a, Description b) {
        return a.getSumInTheInnerPoint() < b.getSumInTheInnerPoint() ? a : b;
    }

    static Description reduce(Description a, Set<Point> pointSet) {
        Description d = new Description();
        pointSet.forEach(p -> d.linkAmount.put(p, a.linkAmount.get(p)));
        d.sumOf = a.sumOf;
        d.makeFinal();
        return d;
    }

    Set<Description> getSumOf() {
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
}


