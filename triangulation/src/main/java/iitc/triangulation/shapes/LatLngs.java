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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LatLngs latLngs = (LatLngs) o;

        if (Double.compare(latLngs.lat, lat) != 0) return false;
        if (Double.compare(latLngs.lng, lng) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
