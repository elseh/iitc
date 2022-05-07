package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.keys.KeysStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * Created by Sigrlinn on 15.06.2015.
 */
public class Description {
    protected Map<Point, Integer> linkAmount = new HashMap<>();
    protected Set<FieldDescription> sumOf = new HashSet<>(); // does not contain outer description
    int linksSum = 0;
    public Description(Set<Point> pointSet) {
        pointSet.forEach(p -> linkAmount.put(p, 0));
        sumLinks();
    }

    public Description(Description base, FieldDescription inner) {
        base.linkAmount.forEach((k, v) -> linkAmount.merge(k, v, Integer::sum));
        //inner.linkAmount.forEach((k, v) -> linkAmount.merge(k, v, Integer::sum));

        inner.linkAmount.forEach((k, v) -> linkAmount.computeIfPresent(k, (key, value) -> value + v));

        sumOf.addAll(base.sumOf);
        sumOf.add(inner);
        sumLinks();
    }

    protected void sumLinks() {
        linksSum = linkAmount.values().stream().mapToInt(v -> v).sum();
    }

    public static Description reverse(Description d) {
        Description description = new Description(d.linkAmount.keySet());
        d.linkAmount.forEach((k, v) -> description.linkAmount.put(k, k.getMaxLinks()-v));
        return description;
    }

    public Set<FieldDescription> getSumOf() {
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
        return Objects.equals(linkAmount, that.linkAmount);
    }

    @Override
    public int hashCode() {
        return linkAmount != null ? linkAmount.hashCode() : 0;
    }

    public boolean goodDescription() {
        return getLinkAmount().entrySet()
            .stream()
            .noneMatch(e -> e.getValue() > e.getKey().getMaxLinks());
    }

    public static Description min(Description a, Description b) {
        return a.linksSum < b.linksSum ? a : b;
    }

    public int getLinksSum() {
        return linksSum;
    }

    @Override
    public String toString() {
        return "[" + String.join(", ", linkAmount.values().stream().map(i -> i + "").collect(Collectors.joining(", ")) + "]");
    }
}


