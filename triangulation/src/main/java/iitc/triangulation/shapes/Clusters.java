package iitc.triangulation.shapes;

import iitc.triangulation.Point;

import java.util.ArrayList;
import java.util.List;

public class Clusters {
    List<List<Point>> clusters = new ArrayList<>();

    public Clusters() {
    }

    public void addCluster(List<Point> cluster) {
        clusters.add(cluster);
    }
}
