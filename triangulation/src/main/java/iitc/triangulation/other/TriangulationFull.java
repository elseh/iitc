package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
public class TriangulationFull {
    private SingleComputer<Set<Point>, CompletableFuture<Void>> descriptionCalculators = new SingleComputer<>();
    private ConcurrentMap<Set<Point>, Set<Description>> allDescriptions = new ConcurrentHashMap<>();
    private AtomicInteger doneDescriptions = new AtomicInteger(0);
    //private final ExecutorService analyze = Executors.newFixedThreadPool(1);
    private AllFields allFields;

    AtomicInteger fieldsToAnalyse = new AtomicInteger(0);
    CompletableFuture<Void> analyzeFinished = new CompletableFuture<>();

    public TriangulationFull(AllFields fields) {
        this.allFields = fields;
    }

    private ExecutorService current;

    private AtomicInteger renew = new AtomicInteger(0);
    private ReentrantLock executorLock = new ReentrantLock();

    private ExecutorService getExecutor() {
        executorLock.lock();
        try {
            int when = renew.get();
            int now = doneDescriptions.get();
            if (current == null || when < now) {
                renew.addAndGet(25000);
                current = Executors.newFixedThreadPool(1);
                System.out.println("new Executor");
            }
            return current;
        } finally {
            executorLock.unlock();
        }

    }

    public CompletableFuture<Void> getAnalyzeFinished() {
        return analyzeFinished;
    }

    /* Analyse */

    public void startBasesProcessing(Set<Triple<Point>> bases) {
        fieldsToAnalyse.addAndGet(bases.size());
        bases.forEach(b -> allFields.get(b.set()).thenAcceptAsync(this::startFieldProcessing, getExecutor()));
    }

    private void startFieldProcessing(Field field) {
        Set<Point> bases = field.getBases().set();
        Optional<AtomicInteger> once = undone.computeOnce(bases, b -> new AtomicInteger(field.getInners().size() * 3));
        if (!once.isPresent()) return;

        field.getInners().stream()
                .map(field::smallerTriangles)
                .flatMap(List::stream)
                .forEach(set -> {
                    requiredFor.computeIfAbsent(set, s -> new ArrayList<>()).add(bases);
                    allFields.get(set).thenAcceptAsync(this::startFieldProcessing, getExecutor());
                });

        finishFieldProcessing(bases);
    }

    Map<Set<Point>, Set<Point>> interned = new HashMap<>();

    private Set<Point> intern(Set<Point> points) {
        return interned.computeIfAbsent(points, p -> points);
    }

    private void finishFieldProcessing(Set<Point> bases) {
        Set<Point> iBases = intern(bases);
        boolean wasDone = undone.get(iBases).compareAndSet(0, -1);
        if (!wasDone) return;
        startAnalyse(iBases).thenRun(() -> notifyWaiters(iBases));
    }

    private void notifyWaiters(Set<Point> bases) {
        List<Set<Point>> waiters = requiredFor.get(bases);
        if (waiters == null) {
            int left = fieldsToAnalyse.decrementAndGet();
            System.out.println("Decremented " + left + " " + doneDescriptions.get());
            if (left == 0) {
                analyzeFinished.complete(null);
            }
            return;
        }
        waiters.forEach(bigger -> {
            int undoneLeft = undone.get(bigger).decrementAndGet();
            if (undoneLeft == 0) {
                CompletableFuture.runAsync(() -> finishFieldProcessing(bigger), getExecutor());
            }
        });
    }

    private CompletableFuture<Void> startAnalyse(Set<Point> set) {
        return descriptionCalculators
                .computeOnce(set, v -> CompletableFuture.runAsync(() -> processField(intern(set)), getExecutor()))
                .orElse(null);
    }

    private Set<Description> analysedField(Set<Point> set) {
        return allDescriptions.get(set);
    }

    private void processField(Set<Point> base) {
        Field field = allFields.get(base).getNow(null);
        AtomicInteger innersToProcess = new AtomicInteger(field.getInners().size());
        Set<Description> values = new HashSet<>();
        if (innersToProcess.get() == 0) {
            values.add(Description.skipAll(field.getBases().set()));
            writeState(base, values);
            return;
        }
        CompletableFuture<Void> allInnersProcessed = new CompletableFuture<>();

        field.getInners()
                .stream()
                .map(p -> sumFields(field, p))
                .forEach(future -> {
                    future.thenAcceptAsync(set -> {
                        values.addAll(set);
                        int left = innersToProcess.decrementAndGet();
                        if (left == 0) {
                            allInnersProcessed.complete(null);
                        }
                    }, getExecutor());
                });

        allInnersProcessed.thenAcceptAsync((v) -> {
            Collection<Description> descriptions = values
                    .stream()
                    .filter(this::goodDescription)
                    .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values();
            writeState(base, new HashSet<>(descriptions));
        }, getExecutor());
    }

    private long last = System.currentTimeMillis();

    private void writeState(Set<Point> base, Set<Description> descriptions) {
        allDescriptions.put(base, descriptions);
        int almostDone = doneDescriptions.incrementAndGet();
        if (almostDone % 100 == 0 || (System.currentTimeMillis() - last > 5 * 1000)) {
            last = System.currentTimeMillis();
            System.out.println(MessageFormat.format(
                    "diff: {0} {1, time, full} {2} almostDone: {3} intern {4}, DC:{5}",
                    descriptionCalculators.size() - almostDone, new Date(), allFields.size(),
                    almostDone, interned.size(), descriptionCalculators.size()));
        }
    }


    private CompletableFuture<Set<Description>> sumFields(Field f, Point inner) {
        Triple<Point> bases = f.getBases();
        Set<Set<Point>> toSum = bases.split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet());

        return CompletableFuture.supplyAsync(() -> sumFields(toSum, Description.makeBase(bases.set())), getExecutor());
    }

    /* Analyse end*/

    /* common */


    private Set<Description> sumFields(Set<Set<Point>> bases, Description baseDescription) {
        Set<Description> base = new HashSet<>();
        Set<Point> pointSet = baseDescription.getLinkAmount().keySet();
        base.add(baseDescription);

        List<Set<Description>> analysedFutures = bases.stream()
                .map(this::analysedField)
                .collect(Collectors.toList());

        Set<Description> result = base;

        for (Set<Description> analysed : analysedFutures) { // supposed to be completed already
            Collection<Description> summed = result.stream()
                    .flatMap(element -> analysed
                            .stream()
                            .map(element::insert))
                    .filter(this::goodDescription)
                    .collect(Collectors.toMap(
                            Description::getLinkAmount,
                            l -> l,
                            Description::min)
                    ).values();
            result = new HashSet<>(summed);
        }

        return new HashSet<>(result.stream()
                .map(d -> Description.reduce(d, pointSet))
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min))
                .values());
    }

    private boolean goodDescription(Description d) {
        return d.getLinkAmount().entrySet()
                .stream()
                .noneMatch(e -> e.getValue() > e.getKey().getMaxLinks()) && d.checkSumInTheInnerPoint();
    }

    /* end common */


    /*post Analyse*/

    public CompletableFuture<Set<Description>> calculateFields(Set<Set<Point>> bases) {
        Set<Point> baseDescriptionSet = bases.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Description bDescription = Description.makeEmptyBase(baseDescriptionSet);
        return CompletableFuture.supplyAsync(() -> sumFields(bases, bDescription), getExecutor());
    }

    private void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (0 == f.getInners().size()) {
            return;
        }
        if (!analysedField(set).contains(d)) {
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

    public void printState() {
        System.out.println("All Descriptions size: " + descriptionCalculators.size());
        System.out.println("Fields size: " + allFields.size());
    }
    /* end post Analyse*/


    private SingleComputer<Set<Point>, AtomicInteger> undone = new SingleComputer<>();
    private ConcurrentMap<Set<Point>, List<Set<Point>>> requiredFor = new ConcurrentHashMap<>();


}
