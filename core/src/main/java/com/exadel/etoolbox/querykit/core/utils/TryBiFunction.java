/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.utils;

/**
 * Represents a functional monad based upon a {@code BiFunction} that throws an exception
 * @param <T> Type of the first argument
 * @param <U> Type of the second argument
 * @param <R> Type of the returned value
 */
@FunctionalInterface
public interface TryBiFunction<T, U, R> {

    /**
     * Executes a bi-argument function
     * @param t First argument
     * @param u Second argument
     * @return {@code R}-typed value
     * @throws Exception If the enclosed code produces an exception
     */
    R apply(T t, U u) throws Exception;
}
