package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

 /**
 * Created by Sigrlinn on 16.06.2015.
 */
@HasValues
public class TriangulationFull {

    @Value("triangulation.full.main_summator.tasks:30")
    private static int MAIN_SUMMATOR_TASKS_NUMBER;
    @Value("triangulation.full.final_summator.tasks:1")
    private static int FINAL_SUMMATOR_TASKS_NUMBER;
    @Value("triangulation.full.process.tasks:1")
    private static int PROCESS_TASKS_NUMBER;

    private AtomicInteger fieldsToAnalyse = new AtomicInteger(0);
    private CompletableFuture<Void> totalAnalyzeFinished;
    private ReentrantLock allDescriptionsLock = new ReentrantLock();
    private Map<Set<Point>, Set<Point>> interned = new HashMap<>();
    private ConcurrentMap<Set<Point>, Set<Description>> allDescriptions = new ConcurrentHashMap<>();
    private ConcurrentMap<FieldToSum, Set<Description>> descriptionsForSplit = new ConcurrentHashMap<>();
    private ConcurrentMap<Field, AtomicInteger> fieldsToSumAmount = new ConcurrentHashMap<>();
    private AtomicInteger doneDescriptions = new AtomicInteger(0);
    private AllFields allFields;
    private BlockingQueue<Set<Point>> queueToProcess = new LinkedBlockingQueue<>();

    private BlockingQueue<FieldToSum> queueToSum = new LinkedBlockingQueue<>();
    private BlockingQueue<Field> queueToFinishSum = new LinkedBlockingQueue<>();
    private long last = System.currentTimeMillis();

    private AtomicInteger descToDone = new AtomicInteger(0);

    public TriangulationFull(AllFields fields) {
        this.allFields = fields;
    }

    public CompletableFuture<Void> getAnalyzeFinished() {
        return totalAnalyzeFinished;
    }

    public void startBasesProcessing() {
        runPusher();
        CompletableFuture<Void> f1 = runSummator(FINAL_SUMMATOR_TASKS_NUMBER);
        CompletableFuture<Void> f3 = runSumFinisher(MAIN_SUMMATOR_TASKS_NUMBER);
        CompletableFuture<Void> f2 = runProcessor(PROCESS_TASKS_NUMBER);

        totalAnalyzeFinished = CompletableFuture.allOf(f2, f1, f3);
        fieldsToAnalyse.addAndGet(allFields.size());

    }

    private BlockingQueue<Integer> pointsAmount = new LinkedBlockingQueue<>();
    private BlockingQueue<Boolean> processNext = new LinkedBlockingQueue<>();
    private void runPusher() {
        allFields.getOrder().keySet().stream().sorted().forEach(pointsAmount::offer);

        new TriCalculationTask<Boolean>(processNext, "Pusher") {

            @Override
            protected boolean isRunning() {
                return !pointsAmount.isEmpty();
            }

            @Override
            protected void process(Boolean nextItem) {
                try {
                    Integer take = pointsAmount.take();
                    List<Set<Point>> sets = allFields.getOrder().get(take);
                    System.out.println(MessageFormat.format("pushing {0} {1} ({2})", take, sets.size(), take * sets.size()));
                    int i = descToDone.addAndGet(sets.size());
                    sets.forEach(f -> {
                        pushToProcess(intern(f));
                    }
                    );
                    if (i == 0) {
                        processNext.offer(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start(1);
        processNext.offer(true);
    }


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

    private void pushToProcess(Set<Point> base) {
        if (!queueToProcess.offer(intern(base))) {
            System.out.println("ALARMA, processingQueue");
        }
    }

    private CompletableFuture<Void> runProcessor(int amount) {
        return new TriCalculationTask<Set<Point>>(queueToProcess, "mainProcessor") {
            Set<Set<Point>> bases = new HashSet<>();
            private AtomicInteger fieldsProcessed = new AtomicInteger(0);

            @Override
            protected void process(Set<Point> nextItem) {
                if (bases.add(nextItem)) {
                    int i = fieldsProcessed.incrementAndGet();
                    if (i % 100 == 0) {
                        System.out.println("Jubilaum (" + getName() + ")" + i + "(" + queue.size() + ")");
                    }
                    processField(nextItem);
                }
            }
        }.start(amount).getFuture();

    }

    private CompletableFuture<Void> runSummator(int amount) {
        return new TriCalculationTask<FieldToSum>(queueToSum, "SingleSummator") {
            @Override
            protected void process(FieldToSum nextItem) {
                Set<Description> value = sumFields(nextItem);

                descriptionsForSplit.put(nextItem, value);
                int i = fieldsToSumAmount.get(nextItem.getField()).decrementAndGet();

                if (i == 0) queueToFinishSum.offer(nextItem.getField());

            }
        }.start(amount).getFuture();
    }

    private CompletableFuture<Void> runSumFinisher(int amount) {
        return new TriCalculationTask<Field>(queueToFinishSum, "FinalSummator") {
            @Override
            protected void process(Field nextItem) {
                Collection<Description> values = nextItem.getInners().stream()
                        .map(p -> new FieldToSum(nextItem, p))
                        .map(descriptionsForSplit::get)
                        .flatMap(Collection::stream)
                        .filter(Description::checkDescriptionGoodness)
                        .collect(Description.TO_MAP).values();
                writeState(nextItem.getBases().set(), new HashSet<>(values));
            }
        }.start(amount).getFuture();
    }

    private void processField(Set<Point> base) {
        Field field = allFields.get(base);
        fieldsToSumAmount.put(field, new AtomicInteger(field.getInners().size()));

        Set<Description> values = new HashSet<>();
        if (field.getInners().isEmpty()) {
            values.add(Description.skipAll(field.getBases().set()));
            writeState(base, values);
            return;
        }

        field.getInners()
                .forEach(p -> toSumOffer(field, p));
    }

    private void toSumOffer(Field field, Point inner) {

        Triple<Point> bases = field.getBases();
        Set<Set<Point>> collect = bases.split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet());
        FieldToSum e = new FieldToSum(field, inner);
        e.setBases(bases);
        e.setCollect(collect);
        queueToSum.offer(e);
    }

    private void writeState(Set<Point> base, Set<Description> descriptions) {
        allDescriptionsLock.lock();
        try {
            allDescriptions.put(base, descriptions);
        } finally {
            allDescriptionsLock.unlock();
        }
        int almostDone = doneDescriptions.incrementAndGet();
        int toDone = descToDone.decrementAndGet();
        if (toDone == 0) {
            processNext.offer(true);
        }
        if (almostDone % 100 == 0 || (System.currentTimeMillis() - last > 5 * 1000)) {
            printState(almostDone);
        }
    }

    private void printState(int almostDone) {
        last = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "diff: {0, time, full} {1} almostDone: {2} intern {3} qTS {4} qTFS {5} lQ {6}",
                new Date(), allFields.size(), almostDone, interned.size(), queueToSum.size(), queueToFinishSum.size(), queueToProcess.size()));
    }

    private Set<Description> sumFields(FieldToSum field) {
        Triple<Point> bases = field.getBases();
        Set<Set<Point>> toSum = field.getCollect();

        return sumFields(toSum, Description.makeBase(bases.set()));
    }

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
                                    .map(element::insert)
                    )
                    .filter(Description::checkDescriptionGoodness)
                    .collect(Description.TO_MAP)
                    .values();
            result = new HashSet<>(summed);
        }

        return new HashSet<>(result.stream()
                .map(d -> Description.reduce(d, pointSet))
                .collect(Description.TO_MAP)
                .values());
    }

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

    private abstract class TriCalculationTask<T> extends CalculationTask<T> {
        private String name;

        TriCalculationTask(BlockingQueue<T> queue, String name) {
            super(queue);
            this.name = name;
        }

        @Override
        protected void printState() {
            TriangulationFull.this.printState(doneDescriptions.get());
        }

        @Override
        protected boolean isRunning() {
            return !queueToSum.isEmpty() ||
                   !queueToFinishSum.isEmpty() ||
                   !queueToProcess.isEmpty() ||
                    doneDescriptions.get() < allFields.size();
        }

        @Override
        protected String getName() {
            return name;
        }
    }

}
