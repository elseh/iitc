package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.Objects;
import java.util.Set;

/**
 * @author epavlova
 * @version 06.12.2018
 */
class FieldToSum {
    Field f;
    Point inner;
    private Triple<Point> bases;
    private Set<Set<Point>> collect;

    public FieldToSum(Field f, Point inner) {
        this.f = f;
        this.inner = inner;
    }

    public Field getField() {
        return f;
    }

    public Point getInner() {
        return inner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldToSum that = (FieldToSum) o;
        return Objects.equals(f, that.f) &&
                Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f, inner);
    }

    public void setBases(Triple<Point> bases) {

        this.bases = bases;
    }

    public void setCollect(Set<Set<Point>> collect) {
        this.collect = collect;
    }

    public Triple<Point> getBases() {
        return bases;
    }

    public Set<Set<Point>> getCollect() {
        return collect;
    }
}
