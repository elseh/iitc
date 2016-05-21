
package iitc.triangulation;

/**
 * Created by Sigrlinn on 14.06.2015.
 */
public class RawData {
    private Point[] points;
    private FieldSerializer.Drawing[] drawings;
    private String name;
    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public FieldSerializer.Drawing[] getDrawings() {
        return drawings;
    }

    public void setDrawings(FieldSerializer.Drawing[] drawings) {
        this.drawings = drawings;
    }

    public String getName() {
        return name;
    }
}