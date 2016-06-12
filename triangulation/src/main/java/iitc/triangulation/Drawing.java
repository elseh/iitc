package iitc.triangulation;

import iitc.triangulation.shapes.LatLngs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author epavlova
 * @version 22.05.2016
 */
public class Drawing {
    private String type = "polyline";
    private List<LatLngs> latLngs;
    private String color = "green";

    public Drawing(String type, Point... points) {
        this.type = type;
        this.latLngs = Arrays.asList(points)
                .stream()
                        //.peek(x -> System.out.println(x))
                .map(Point::getLatlng)
                .collect(Collectors.toList());
    }

    public String getType() {
        return type;
    }

    public List<LatLngs> getLatLngs() {
        return latLngs;
    }

    public String getColor() {
        return color;
    }

    public Drawing setColor(String color) {
        this.color = color;
        return this;
    }
}
