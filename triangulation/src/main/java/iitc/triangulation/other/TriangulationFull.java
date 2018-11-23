package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
public class TriangulationFull {
    private ConcurrentMap<Set<Point>, Set<Description>> allDescriptions = new ConcurrentHashMap<>();
    private AtomicInteger doneDescriptions = new AtomicInteger(0);
    private AllFields allFields;

    AtomicInteger fieldsToAnalyse = new AtomicInteger(0);
    CompletableFuture<Void> totalAnalyzeFinished;

    ReentrantLock allDescriptionsLock = new ReentrantLock();

    public TriangulationFull(AllFields fields) {
        this.allFields = fields;
    }

    private ExecutorService current;

    private ReentrantLock executorLock = new ReentrantLock();

    private ExecutorService getExecutor() {
        executorLock.lock();
        try {
            if (current == null) {
                current = Executors.newFixedThreadPool(165);
                System.out.println("new Executor");
            }
            return current;
        } finally {
            executorLock.unlock();
        }

    }

    public CompletableFuture<Void> getAnalyzeFinished() {
        return totalAnalyzeFinished;
    }

    /* Analyse */

    public void startBasesProcessing(Set<Triple<Point>> bases) {
        CompletableFuture<Void> f2 = runProcessor(1);
        totalAnalyzeFinished = CompletableFuture.allOf(f2);
        fieldsToAnalyse.addAndGet(allFields.size());
        List<Set<Point>> collect = allFields.getOrder().keySet().stream().sorted()
                .map(allFields.getOrder()::get)
                .flatMap(Collection::stream).collect(Collectors.toList());
        collect.forEach(b -> analysedFutures.put(b, new CompletableFuture<>()));
        collect.forEach(this::pushToProcess);
    }

  Map<Set<Point>, Set<Point>> interned = new HashMap<>();

    private Set<Point> intern(Set<Point> points) {
        return interned.computeIfAbsent(points, p -> points);
    }

    private Set<Description> analysedField(Set<Point> set) {
        allDescriptionsLock.lock();
        try {
            return allDescriptions.get(set);
        } finally {
            allDescriptionsLock.unlock();
        }
    }

    private BlockingQueue<Set<Point>> queueToProcess = new LinkedBlockingQueue<>();
    private void pushToProcess(Set<Point> base) {

        if (!queueToProcess.offer(intern(base))) {
            System.out.println("ALARMA, processingQueue");
        }
    }

    private CompletableFuture<Void> runProcessor(int amount) {
        Task<Set<Point>> finisher = new Task<Set<Point>>(queueToProcess) {
            private AtomicInteger fieldsProcessed = new AtomicInteger(0);
            Set<Set<Point>> bases = new HashSet<>();
            @Override
            protected boolean isRunning() {
                return allFields.size()-fieldsProcessed.get() > 0;
            }

            @Override
            protected void process(Set<Point> nextItem) {
                if (bases.add(nextItem)) {
                    int i = fieldsProcessed.incrementAndGet();
                    if (i % 100 == 0) {
                        System.out.println("Jubilaum (" + getName()+ ")" + i + "(" + queue.size() + ")");
                    }
                    processField(nextItem);
                }
            }

            @Override
            protected String getName() {
                return "To process";
            }
        };
        for (int i = 0; i < amount; i++)
            new Thread(finisher::execute).start();
        return finisher.getFuture();
    }

    private void processField(Set<Point> base) {
        Field field = allFields.get(base);
        AtomicInteger innersToProcess = new AtomicInteger(field.getInners().size());
        Set<Description> values = new HashSet<>();
        if (innersToProcess.get() == 0) {
            values.add(Description.skipAll(field.getBases().set()));
            writeState(base, values);
            return;
        }

        CompletableFuture<Void> allProcessed = new CompletableFuture<>();
        field.getInners()
                .forEach(p ->
                        CompletableFuture.runAsync(() -> {
                            Set<Description> descriptions = sumFields(field, p);
                            values.addAll(descriptions);
                            int left = innersToProcess.decrementAndGet();
                            if (left == 0) {
                                allProcessed.complete(null);
                            }
                        }, getExecutor())
                );
        allProcessed.thenRunAsync(() -> {
            Collection<Description> descriptions = values
                    .stream()
                    .filter(Description::checkDescriptionGoodness)
                    .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values();
            writeState(base, new HashSet<>(descriptions));
                }, getExecutor()
        ).join();
    }

    private long last = System.currentTimeMillis();

    private void writeState(Set<Point> base, Set<Description> descriptions) {
        allDescriptionsLock.lock();
        try {
            allDescriptions.put(base, descriptions);
            analysedFutures.get(intern(base)).complete(null);
        } finally {
            allDescriptionsLock.unlock();
        }
        int almostDone = doneDescriptions.incrementAndGet();
        if (almostDone % 100 == 0 || (System.currentTimeMillis() - last > 5 * 1000)) {
            last = System.currentTimeMillis();
            System.out.println(MessageFormat.format(
                    "diff: {0, time, full} {1} almostDone: {2} intern {3}",
                    new Date(), allFields.size(), almostDone, interned.size()));
        }
    }


    private Set<Description> sumFields(Field f, Point inner) {
        Triple<Point> bases = f.getBases();
        Set<Set<Point>> toSum = bases.split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet());

        return sumFields(toSum, Description.makeBase(bases.set()));
    }

    /* Analyse end*/

    /* common */

    private Map<Set<Point>, CompletableFuture<Void>> analysedFutures = new HashMap<>();
    private Set<Description> sumFields(Set<Set<Point>> bases, Description baseDescription) {
        Set<Description> base = new HashSet<>();
        Set<Point> pointSet = baseDescription.getLinkAmount().keySet();
        base.add(baseDescription);
        CompletableFuture[] toArray = bases.stream().map(b -> analysedFutures.get(b)).toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(toArray).join();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        List<Set<Description>> analysedFutures = bases.stream()
                .map(this::analysedField)
                .collect(Collectors.toList());
        Set<Description> result = base;

        for (Set<Description> analysed : analysedFutures) { // supposed to be completed already
            Collection<Description> summed = result.stream()
                    .flatMap(element -> analysed
                            .stream()
                            .map(element::insert))
                    .filter(Description::checkDescriptionGoodness)
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

    /* end common */


    /*post Analyse*/

    public Set<Description> calculateFields(Set<Set<Point>> bases) {
        Set<Point> baseDescriptionSet = bases.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Description bDescription = Description.makeEmptyBase(baseDescriptionSet);
        bDescription.makeFinal();
        return sumFields(bases, bDescription);
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
        System.out.println("Fields size: " + allFields.size());
    }
    /* end post Analyse*/

    private abstract class Task<T> {
        BlockingQueue<T> queue;
        private CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicInteger counter = new AtomicInteger();
        public Task(BlockingQueue<T> queue) {
            this.queue = queue;
        }

        void execute() {
            counter.incrementAndGet();
            while (isRunning()) {
                T nextItem = null;
                try {
                    nextItem = queue.take();
                    process(nextItem);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            System.out.println(getName() + " is finished " + queue.size());
            int f = counter.decrementAndGet();
            if ( f == 0) {
                future.complete(null);
            }
        }

        public CompletableFuture<Void> getFuture() {
            return future;
        }

        protected abstract boolean isRunning();
        protected abstract void process(T nextItem);

        protected abstract String getName();
    }
}
