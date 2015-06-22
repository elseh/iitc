package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Pair;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 21.06.2015.
 */
public class FrameGenerator {
    public Optional<Map<Point, Set<Point>>> makeFrame(Description d, List<Triple<Point>> bases) {
        Description reverse = Description.reverse(d);
        Set<LinkSmall> links = bases
                .stream()
                .flatMap(b -> b.split().stream())
                .map(LinkSmall::new)
                .collect(Collectors.toSet());
        Map<Point, Set<Point>> r = new HashMap<>();


        return Optional.empty();
    }


    private boolean same(Link a, Link b) {
        return (a.getFrom().equals(b.getFrom()) && a.getTo().equals(b.getTo())) ||
                (a.getFrom().equals(b.getTo()) && a.getTo().equals(b.getFrom()));
    }

    private class LinkSmall {
        Pair<Point> pair;
        boolean reverse;

        public LinkSmall(Pair<Point> pair) {
            this.pair = pair;
        }

        @Override
        public boolean equals(Object o) {
            return  (((LinkSmall) o).pair == pair) || (((LinkSmall) o).pair == pair.reverse());
        }

        @Override
        public int hashCode() {
            return pair != null ? pair.hashCode() : 0;
        }

        public boolean has(Point p) {
            return pair.v1 == p || pair.v2 == p;
        }
    }

}
