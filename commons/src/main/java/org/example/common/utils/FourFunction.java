package org.example.common.utils;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface FourFunction<T, U, V, I, R> {

    R apply(T t, U u, V v, I i);

    default <K> FourFunction<T, U, V, I,K> andThen(Function<? super R, ? extends K> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v, I i) -> after.apply(apply(t, u, v, i));
    }
}