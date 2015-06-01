package iitc.triangulation;

import iitc.triangulation.shapes.LatLngs;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Point {
    String id;
    String title;
    LatLngs latlng;

    public Point(String id, String title, LatLngs latlng) {
        this.id = id;
        this.title = title;
        this.latlng = latlng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLngs getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLngs latlng) {
        this.latlng = latlng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return id.equals(point.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
