package iitc.triangulation;

/**
 * @author epavlova
 * @version 22.05.2016
 */
public class Utils {
    public String toURL(Point p) {
        return "https://www.ingress.com/intel?ll={0},{1}&z=17&pll={0},{1}"
                .replaceAll("[{]0[}]", p.latlng.getLat() + "")
                .replaceAll("[{]1[}]", p.latlng.getLng() + "");
    }

    public double length(Point p1, Point p2) {
        return DeployOrder.length(p1, p2);
    }

    public String tab() {return "\t";}
}
