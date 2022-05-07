package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.shapes.Pair;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;
import org.apache.logging.log4j.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
@HasValues
public class TriangulationFull {
    @Value("cutOff:100") private static int cutOff;

    private static final Logger log = LogManager.getLogger(TriangulationFull.class);
    private HashMap<Set<Point>, Set<FieldDescription>> allDescriptions = new HashMap<>();
    private FieldsStorage storage = new FieldsStorage();

    private List<Point> allPoints;

    public TriangulationFull(List<Point> allPoints) {
        this.allPoints = allPoints;
    }

    public Set<FieldDescription> analyseSingleField(Set<Point> set) {
        Field field = storage.get(set);

        if (allDescriptions.containsKey(set)) {
            return allDescriptions.get(set);
        }

        Set<FieldDescription> values = new HashSet<>(field.getInners()
                .stream()
                .flatMap(p -> sumFields(field, p).stream())
                .filter(Description::goodDescription)
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, FieldDescription::min)).values());
        if (field.getInners().size() < 1) {
            values.add(new FieldDescription(null, set));
        }
        allDescriptions.put(set, values);
        if (allDescriptions.size() % 100 == 0) {
            log.info("size: {} ", allDescriptions.size());
        }
        return values;
    }

    public Set<Description> calculateFields(Set<Set<Point>> bases) {
        Set<Point> baseDescriptionSet = bases.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Description bDescription = new Description(baseDescriptionSet);
        return sumFields(bases, bDescription, Description::new, Description::min);
    }

    private Set<FieldDescription> sumFields(Field f, Point inner) {
        return sumFields(f.getBases().split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet()),
                new FieldDescription(inner, f.getBases().set()),
            FieldDescription::new,
            FieldDescription::min
        );
    }

    private <T extends Description> Set<T> sumFields(Set<Set<Point>> bases, T baseDescription,
                                                     BiFunction<T, FieldDescription, T> join,
                                                     BinaryOperator<T> min)
    {
        Set<T> base = new HashSet<>();
        base.add(baseDescription);
        bases
            .stream()
            .map(this::analyseSingleField)
            .forEach(
                set -> {
                    Collection<T> values = base.stream()
                        .flatMap(element -> set
                            .stream()
                            .map(in -> join.apply(element, in)))
                        .filter(Description::goodDescription)
                        .sorted(Comparator.comparingInt(Description::getLinksSum))
                        .limit(cutOff)
                        .collect(Collectors.toMap(
                            Description::getLinkAmount,
                            a -> a,
                            min
                            )
                        )
                        .values();
                    base.clear();
                    base.addAll(values);
                }
            );
        if (base.size() == 0) {
            log.debug("empty for bases: {} \n description: {}", bases, baseDescription);
//            throw new IllegalStateException("empty");
        }
        return new HashSet<>(base
                .stream()
                .collect(Collectors.toMap(Description::getLinkAmount,
                    a -> a,
                    min))
                .values());
    }

    public void restore(FieldDescription d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (0 == f.getInners().size()) {
            return;
        }
        if (!analyseSingleField(set).contains(d)) {
            return;
        }
        Point p = d.getInnerPoint();
        f.insertSmallerFields(p);
        Map<Set<Point>, FieldDescription> small = d.getSumOf()
            .stream()
            .collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
        f.getSmallerFields().stream().forEach(sm -> restore(small.get(sm.getBases().set()), sm));
    }

    public void restore(Description d, List<Field> fields) {
        Map<Set<Point>, FieldDescription> small = d.getSumOf().stream().collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
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
