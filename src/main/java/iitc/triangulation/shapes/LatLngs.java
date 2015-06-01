package iitc.triangulation.shapes;


/**
* Created by epavlova on 6/1/2015.
*/
public class LatLngs {
    private double lat, lng;

    public LatLngs(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
