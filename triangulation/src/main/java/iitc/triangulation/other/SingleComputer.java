package iitc.triangulation.other;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author epavlova
 * @version 20.09.2018
 */
class SingleComputer<K, V> {
    private ConcurrentMap<K, V> map;

    public SingleComputer() {
        this.map = new ConcurrentHashMap<>();
    }

    public Optional<V> computeOnce(K key, Function<? super K, ? extends V> mappingFunction) {
        AtomicBoolean b = new AtomicBoolean(false);
        V v = map.computeIfAbsent(key, k -> {
            b.set(true);
            return mappingFunction.apply(k);
        });
        return b.get() ? Optional.of(v) : Optional.empty();
    }

    public V get(K key) {
        return map.get(key);
    }

    public int size() {
        return map.size();
    }

    public int countFilter(Predicate<V> p) {
        AtomicInteger i = new AtomicInteger(0);
        map.forEach((k, v) -> {if (p.test(v)) i.incrementAndGet();});
        return i.get();
    }

    public void filterKeysAndApply(Predicate<V> p, Consumer<K> c) {
        map.forEach((k, v) -> {if (p.test(v)) c.accept(k);});
    }
}
