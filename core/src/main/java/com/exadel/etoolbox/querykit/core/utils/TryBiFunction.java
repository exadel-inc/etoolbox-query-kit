package com.exadel.etoolbox.querykit.core.utils;

@FunctionalInterface
public interface TryBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}
