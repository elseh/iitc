package iitc.triangulation;

import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epavlova on 6/2/2015.
 */
public class DeployOrder {
    private Field mainField;

    public DeployOrder(Field mainField) {
        this.mainField = mainField;
    }

    public void calculate() {
        Triple<Point> bases = mainField.getBases();
        List<Point> result = new ArrayList<>();
        result.add(bases.v3);
        result.add(bases.v1);
        result.addAll(analyseField(mainField, bases.v2, true));
        result.stream().forEach(p -> System.out.println(p.getTitle()));
    }

    public List<Point> analyseField(Field field, Point upper, boolean isCenter) {
        List<Point> result = new ArrayList<>();
        if (!field.getInners().isEmpty()) {
            Point innerPoint = field.getInnerPoint();
            Triple<Field> smallerFields = field.getSmallerFields();
            smallerFields.stream().filter(f -> f.getBases().stream().allMatch(p -> !p.equals(upper))).forEach(f -> result.addAll(analyseField(f, innerPoint, true)));
            smallerFields.stream().filter(f -> f.getBases().stream().anyMatch(p -> p.equals(upper))).forEach(f -> result.addAll(analyseField(f, upper, false)));
        }
        if (isCenter) {
            result.add(upper);
        }
        return result;
    }


}
