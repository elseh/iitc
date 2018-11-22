package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author epavlova
 * @version 06.09.2018
 */
public class AllFields {
    private SingleComputer<Set<Point>, CompletableFuture<Field>> allFields = new SingleComputer<>();

    private Executor executor = Executors.newFixedThreadPool(40);
    private final AtomicInteger fieldsCounter = new AtomicInteger(0);
    private final AtomicReference<CompletableFuture<Void>> fieldsComplete = new AtomicReference<>(new CompletableFuture<>());

    public AllFields() {
    }

    public CompletableFuture<Field> get(Set<Point> set) {
        return allFields.get(set);
    }


    private CompletableFuture<Field> onAbsent(Set<Point> set, List<Point> all) {
        inc();
        return CompletableFuture.supplyAsync(() -> {
            Point[] points = set.toArray(new Point[3]);
            Triple<Point> t = Triple.of(points[0], points[1], points[2]);
            return new Field(t, all);
        }, executor);
    }

    private void inc() {
        fieldsCounter.incrementAndGet();
    }

    private void dec() {
        synchronized (fieldsCounter) {
            int decrement = fieldsCounter.decrementAndGet();
            if (decrement == 0) fieldsComplete.get().complete(null);
        }
    }

    public CompletableFuture<Void> onComplete() {
        synchronized (fieldsCounter) {
            if (fieldsComplete.get().isDone() && fieldsCounter.get() > 0) {
                fieldsComplete.set(new CompletableFuture<Void>());
            }
        }
        return fieldsComplete.get();
    }

    public int size() {
        return allFields.size();
    }


    public void pushBases(Set<Triple<Point>> bases, List<Point> all) {
        bases.forEach(b -> buildAllFields(b.set(), all));
    }

    private void buildAllFields(Set<Point> set, List<Point> all) {
        allFields
                .computeOnce(set, s -> onAbsent(set, all))
                .ifPresent(f -> {
                    f.thenAccept(this::buildNewFields);
                });
    }

    private void buildNewFields(Field field) {
        field.getInners().stream()
                .map(field::smallerTriangles)
                .flatMap(Collection::stream)
                .forEach(set -> CompletableFuture.runAsync(() -> buildAllFields(set, field.getInners())));
        dec();
    }




}
