package iitc.triangulation.other;

import iitc.triangulation.Point;

import java.util.Objects;
import java.util.Set;

public class FieldDescription extends Description {
    private Point innerPoint;
    private int sum = 0;

    public FieldDescription(Point inner, Set<Point> pointSet) {
        super(pointSet);
        innerPoint = inner;
        if (inner != null) {
            pointSet.forEach(p -> linkAmount.put(p, 1));
            sumLinks();
        }

    }

    public FieldDescription(FieldDescription base, FieldDescription inner) {
        super(base, inner);

        innerPoint = base.innerPoint;
        sum = base.sum + inner.sum;
    }

    public boolean checkSumInTheInnerPoint() {
        if (innerPoint == null) return true;
        return sum <= innerPoint.getMaxLinks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FieldDescription that = (FieldDescription) o;
        return sum == that.sum && Objects.equals(innerPoint, that.innerPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerPoint, sum);
    }

    public static FieldDescription min(FieldDescription a, FieldDescription b) {
        if (a.sum == b.sum) return a.linksSum < b.linksSum ? a : b;
        return Math.abs(a.sum-5) < Math.abs(b.sum-5) ? a : b;
    }

    public Point getInnerPoint() {
        return innerPoint;
    }

    public boolean goodDescription() {
        return super.goodDescription() && checkSumInTheInnerPoint();
    }
}
