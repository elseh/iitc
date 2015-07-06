package iitc.triangulation.shapes;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
* Created by epavlova on 6/1/2015.
*/
public class Pair<T> {
    public final T v1, v2;

    private Pair(T v1, T v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public static <T> Pair<T> of(T v1, T v2) {
        return new Pair<>(v1, v2);
    }

    public Pair<T> reverse() {
        return of(v2, v1);
    }

    public <K> Pair<K> simplify(Function<T, K> function) {
        return Pair.of(function.apply(v1), function.apply(v2));
    }

    public Stream<T> stream() {
        return Stream.of(v1, v2);
    }
}
