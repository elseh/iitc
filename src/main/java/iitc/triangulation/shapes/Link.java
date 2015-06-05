package iitc.triangulation.shapes;

/**
 * Created by Sigrlinn on 05.06.2015.
 */
public class Link {
    String name;
    LatLngs from;
    LatLngs to;
    boolean isReverse;

    public Link(String name, LatLngs from, LatLngs to, boolean isReverse) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.isReverse = isReverse;
    }

    public String getName() {
        return name;
    }

    public LatLngs getFrom() {
        return from;
    }

    public LatLngs getTo() {
        return to;
    }

    public boolean isReverse() {
        return isReverse;
    }
}
