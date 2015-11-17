package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 21.06.2015.
 */
public class FrameGenerator {
    public Optional<Map<Point, Set<Point>>> makeFrame(Description d, Set<Triple<Point>> bases) {
        Description reverse = Description.reverse(d);
        Set<LinkSmall> links = bases
                .stream()
                .flatMap(b -> b.split().stream())
                .map(LinkSmall::new)
                .collect(Collectors.toSet());
        if (process(reverse, links).isPresent()) {
            return Optional.of(links.stream().collect(Collectors.groupingBy(l -> l.pair.v1, HashMap::new, Collectors.mapping(l -> l.pair.v2, Collectors.toSet()))));
        }


        return Optional.empty();
    }

    private Optional<Set<LinkSmall>> process(Description reverse, Set<LinkSmall> unprocessed) {
        unprocessed = new HashSet<>(unprocessed);
        Map<Point, Set<LinkSmall>> grouped = group(unprocessed);
        Set<LinkSmall> processed = new HashSet<>();
        while (hasFixed(reverse, grouped)) {
            if (hasImpossible(reverse, unprocessed)) return Optional.empty();
            Set<LinkSmall> outLinks = getOutLinks(reverse, unprocessed);
            processed.addAll(outLinks);
            unprocessed.removeAll(outLinks);
            reverse = substract(reverse, outLinks);

            Set<LinkSmall> inLinks = getInLinks(reverse, unprocessed);
            processed.addAll(inLinks);
            unprocessed.removeAll(inLinks);
            reverse = substract(reverse, inLinks);
            grouped = group(unprocessed);
        }
        if (unprocessed.isEmpty()) return Optional.of(processed);
        LinkSmall linkSmall = unprocessed.stream().findAny().get();
        unprocessed.remove(linkSmall);
        processed.add(linkSmall);
        Description optReverse = substract(reverse, linkSmall);
        Optional<Set<LinkSmall>> set = process(optReverse, unprocessed);
        if (set.isPresent()) {
            processed.addAll(set.get());
            return Optional.of(processed);
        }
        linkSmall.pair = linkSmall.pair.reverse();
        optReverse = substract(reverse, linkSmall);
        set = process(optReverse, unprocessed);
        if (set.isPresent()) {
            processed.addAll(set.get());
            return Optional.of(processed);
        }
        return Optional.empty();
    }

    private Set<LinkSmall> getOutLinks(Description reverse, Set<LinkSmall> unprocessed) {
        Map<Point, Set<LinkSmall>> grouped = group(unprocessed);
        Set<Point> outPoints = grouped.keySet()
                .stream()
                .filter(p -> reverse.getLinkAmount().get(p) == 0)
                .collect(Collectors.toSet());
        Set<LinkSmall> outLinks = unprocessed
                .stream()
                .filter(l -> outPoints.contains(l.pair.v2) || outPoints.contains(l.pair.v1))
                .collect(Collectors.toSet());
        outLinks
                .stream()
                .filter(l -> outPoints.contains(l.pair.v1))
                .forEach(l -> l.pair = l.pair.reverse());
        return outLinks;
    }

    private Set<LinkSmall> getInLinks(Description reverse, Set<LinkSmall> unprocessed) {
        Map<Point, Set<LinkSmall>> grouped = group(unprocessed);
        Set<Point> inPoints = grouped.keySet()
                .stream()
                .filter(p -> reverse.getLinkAmount().get(p) == grouped.get(p).size())
                .collect(Collectors.toSet());
        Set<LinkSmall> inLinks = unprocessed
                .stream()
                .filter(l -> inPoints.contains(l.pair.v1)/* || inPoints.contains(l.pair.v2)*/)
                .collect(Collectors.toSet());
        /*inLinks
                .stream()
                .filter(l -> inPoints.contains(l.pair.v2))
                .forEach(l -> l.pair = l.pair.reverse());*/
        return inLinks;
    }

    private Description substract(Description reverse, Set<LinkSmall> outLinks) {
        Description d = Description.makeEmptyBase(reverse.getLinkAmount().keySet());
        reverse.getLinkAmount().forEach((k, v) -> d.getLinkAmount().put(k, v));
        group(outLinks).forEach((k, v) -> d.getLinkAmount().put(k, reverse.getLinkAmount().get(k) - v.size()));
        return d;
    }

    private Description substract(Description reverse, LinkSmall link) {
        Description d = Description.makeEmptyBase(reverse.getLinkAmount().keySet());
        reverse.getLinkAmount().forEach((k, v) -> d.getLinkAmount().put(k, v));
        d.getLinkAmount().put(link.pair.v1, d.getLinkAmount().get(link.pair.v1) - 1);
        return d;
    }

    private boolean hasImpossible(Description reverse, Set<LinkSmall> links) {
        return links
                .stream()
                .filter(l -> reverse.getLinkAmount().get(l.pair.v1) == 0)
                .filter(l -> reverse.getLinkAmount().get(l.pair.v2) == 0).findAny().isPresent();
    }

    private boolean hasFixed(Description reverse, Map<Point, Set<LinkSmall>> grouped) {
        return grouped.keySet()
                .stream()
                .filter(p -> reverse.getLinkAmount().get(p) == 0 || reverse.getLinkAmount().get(p) == grouped.get(p).size())
                .findAny().isPresent();
    }

    private Map<Point, Set<LinkSmall>> group(Set<LinkSmall> links) {
        return links.stream().collect(Collectors.groupingBy(l -> l.pair.v1, Collectors.toSet()));
    }


    private class LinkSmall {
        Pair<Point> pair;

        public LinkSmall(Pair<Point> pair) {
            this.pair = pair;
        }

        @Override
        public boolean equals(Object o) {
            return  (((LinkSmall) o).pair == pair) || (((LinkSmall) o).pair == pair.reverse());
        }

        @Override
        public int hashCode() {
            return pair.v1.hashCode() + pair.v2.hashCode();
        }

        public boolean has(Point p) {
            return pair.v1 == p || pair.v2 == p;
        }
    }

}
