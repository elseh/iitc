package iitc.triangulation.shapes;

import iitc.triangulation.Point;

/**
 * Created by Sigrlinn on 05.06.2015.
 */
public class Link {
    String name;
    String from;
    String to;
    boolean isReverse;

    public Link(String name, String from, String to, boolean isReverse) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.isReverse = isReverse;
    }

    public Link(Point f, Point t) {
        this(f.getTitle() + " - " + t.getTitle() , f.getId(), t.getId(), false);
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isReverse() {
        return isReverse;
    }
}
