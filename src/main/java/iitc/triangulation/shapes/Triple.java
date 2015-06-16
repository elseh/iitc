package iitc.triangulation.shapes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
* Created by epavlova on 6/1/2015.
*/
public class Triple<T> {
    public final T v1, v2, v3;

    private Triple(T v1, T v2, T v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public static <T> Triple<T> of(T v1, T v2, T v3) {
        return new Triple<>(v1, v2, v3);
    }

    public static <T> Triple<T> of(T v1, Pair<T> pair) {
        return of(v1, pair.v1, pair.v2);
    }


    public List<Pair<T>> split() {
        return Arrays.asList(Pair.of(v1, v2), Pair.of(v2, v3), Pair.of(v3, v1));
    }

    public <K> Triple<K> simplify(Function<T, K> function) {
        return Triple.of(function.apply(v1), function.apply(v2), function.apply(v3));
    }

    public Stream<T> stream() {
        return Arrays.asList(v1, v2, v3).stream();
    }

    public Set<T> set() {
        return stream().collect(toSet());
    }
}
