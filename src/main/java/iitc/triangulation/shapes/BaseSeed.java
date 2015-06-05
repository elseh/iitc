package iitc.triangulation.shapes;

import iitc.triangulation.Point;

import java.util.List;

/**
 * Created by Sigrlinn on 05.06.2015.
 */
public class BaseSeed {
    private List<Triple<Point>> bases;
    private List<Point> points;
    private List<Link> links;

    public BaseSeed(List<Triple<Point>> bases, List<Point> points, List<Link> links) {
        this.bases = bases;
        this.points = points;
        this.links = links;
    }

    public List<Triple<Point>> getBases() {
        return bases;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<Link> getLinks() {
        return links;
    }
}
