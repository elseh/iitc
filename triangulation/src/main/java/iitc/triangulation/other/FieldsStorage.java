package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class FieldsStorage {
  private final HashMap<Set<Point>, Field> allFields = new HashMap<>();
  private Map<Integer, List<Set<Point>>> mapBySize = new HashMap<>();
  public void addField(Set<Point> bases, Function<Field, Stream<Set<Point>>> fieldGenerator, List<Point> all) {
    if (allFields.containsKey(bases)) return;
    Point[] points = bases.toArray(new Point[3]);
    Triple<Point> base = Triple.of(points[0], points[1], points[2]);
    Field field = new Field(base, all);
    allFields.put(bases, field);
    mapBySize.computeIfAbsent(field.getInners().size(), k -> new ArrayList<>()).add(bases);
    fieldGenerator.apply(field).forEach(b -> addField(b, fieldGenerator, field.getInners()));
  }

  public Field get(Set<Point> bases) {
    return allFields.get(bases);
  }

  public Stream<List<Set<Point>>> streamBySize() {
    return mapBySize.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue);
  }

  public int size() {
    return allFields.size();
  }
}
