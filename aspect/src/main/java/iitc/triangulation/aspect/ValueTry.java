package iitc.triangulation.aspect;

/**
 * @author epavlova
 * @version 06.06.2016
 */
@HasValues
public class ValueTry {
    @Value("t1:111")
    private int test1;
    @Value("t2")
    private String test2;

    @Value("v1:static!!!")
    private static String tost;

    @HasValues
    public ValueTry() {
        System.out.println("help me!");
    }

    @Override
    public String toString() {
        return "ValueTry{" +
                "test1=" + test1 +
                ", test2='" + test2 + "\', tost='" + tost +
                "'}";
    }
}
