package org.example.common.utils;


import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface FourConsumer<T, U, V, I> {
    void accept(T t, U u, V v, I i);

}
