package iitc.triangulation.other;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author epavlova
 * @version 07.12.2018
 */
abstract class CalculationTask<T> {
    BlockingQueue<T> queue;
    private CompletableFuture<Void> future = new CompletableFuture<>();
    private AtomicInteger counter = new AtomicInteger();

    CalculationTask(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    private void execute() {
        counter.incrementAndGet();
        while (isRunning() || !queue.isEmpty()) {
            T nextItem;
            try {
                nextItem = queue.poll(5, TimeUnit.SECONDS);
                if (nextItem != null) {
                    process(nextItem);
                } else {
                    System.out.println("skipping " + getName());
                    printState();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println(getName() + " is finished " + queue.size());
        if (counter.decrementAndGet() == 0) {
            future.complete(null);
        }
    }

    CalculationTask<T> start(int number) {
        while (number-- > 0)
            new Thread(this::execute).start();
        return this;
    }

    protected abstract void printState();

    CompletableFuture<Void> getFuture() {
        return future;
    }

    protected abstract boolean isRunning();

    protected abstract void process(T nextItem);

    protected abstract String getName();
}
