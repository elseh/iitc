package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Pair;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;
import org.apache.logging.log4j.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
public class TriangulationFull {
    private static final Logger log = LogManager.getLogger(TriangulationFull.class);
    private HashMap<Set<Point>, Set<Description>> allDescriptions = new HashMap<>();
    private FieldsStorage storage = new FieldsStorage();

    private List<Point> allPoints;

    public TriangulationFull(List<Point> allPoints) {
        this.allPoints = allPoints;
    }

    public Set<Description> analyseSingleField(Set<Point> set) {
        Field field = storage.get(set);

        if (allDescriptions.containsKey(set)) {
            return allDescriptions.get(set);
        }

        Set<Description> values = new HashSet<>(field.getInners()
                .stream()
                .flatMap(p -> sumFields(field, p).stream())
                .filter(this::goodDescription)
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values());
        if (field.getInners().size() < 1) {
            values.add(Description.skipAll(set));
        }
        allDescriptions.put(set, values);
        if (allDescriptions.size() % 100 == 0) {
            log.info("size: {} ", allDescriptions.size());
        }
        return values;
    }

    public Set<Description> calculateFields(Set<Set<Point>> bases) {
        Set<Point> baseDescriptionSet = bases.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Description bDescription = Description.makeEmptyBase(baseDescriptionSet);
        return sumFields(bases, bDescription);
    }

    private Set<Description> sumFields(Field f, Point inner) {
        return sumFields(f.getBases().split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet()),
                Description.makeBase(f.getBases().set()));
    }

    private Set<Description> sumFields(Set<Set<Point>> bases, Description baseDescription) {
        Set<Description> base = new HashSet<>();
        Set<Point> pointSet = baseDescription.getLinkAmount().keySet();
        base.add(baseDescription);
        bases
                .stream()
                .map(this::analyseSingleField).forEach(
                set -> {
                    Collection<Description> values = base.stream()
                            .flatMap(element -> set
                                    .stream()
                                    .map(element::insert))
                            .filter(this::goodDescription)
                            .collect(Collectors.toMap(
                                            Description::getLinkAmount,
                                            a -> a,
                                            Description::min
                                    )
                            )
                                    .values();
                    base.clear();
                    base.addAll(values);
                }
        );
        return new HashSet<>(base
                .stream()
                .map(d -> Description.reduce(d, pointSet))
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min))
                .values());
    }

    private boolean goodDescription(Description d) {
        return d.getLinkAmount().entrySet()
                .stream()
                .noneMatch(e -> e.getValue() > e.getKey().getMaxLinks()) && d.checkSumInTheInnerPoint();
    }

    public void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (0 == f.getInners().size()) {
            return;
        }
        if (!analyseSingleField(set).contains(d)) {
            return;
        }
        Point p = d.getSumOf()
                .stream()
                .flatMap(s -> s.getLinkAmount().keySet().stream())
                .filter(s -> !set.contains(s))
                .findAny().get();
        f.insertSmallerFields(p);
        Map<Set<Point>, Description> small = d.getSumOf().stream().collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
        f.getSmallerFields().stream().forEach(sm -> restore(small.get(sm.getBases().set()), sm));
    }

    public void restore(Description d, List<Field> fields) {
        Map<Set<Point>, Description> small = d.getSumOf().stream().collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
        fields.forEach(sm -> restore(small.get(sm.getBases().set()), sm));
    }

    public void pushBase(Set<Point> set) {
        storage.addField(set,
            field -> {
                List<Pair<Point>> pairs = field.getBases().split();
                return field.getInners().stream()
                    .flatMap(p -> pairs.stream().map(pair -> Triple.of(p, pair)))
                    .map(Triple::set);
            },
            allPoints);
    }

    public void startComputation() {
        Instant start = Instant.now();
        storage.streamBySize().forEach(list -> {
            log.info("To compute: {} of ({})", list.size(), storage.size());
            list.forEach(this::analyseSingleField);
        });
        log.info("Everything computed in : {}", Duration.between(start, Instant.now()));
    }
}
