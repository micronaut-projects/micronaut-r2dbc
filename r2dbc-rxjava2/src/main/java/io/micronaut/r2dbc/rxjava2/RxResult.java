/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.r2dbc.rxjava2;

import io.micronaut.core.annotation.Experimental;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.reactivex.Flowable;
import java.util.function.BiFunction;


/**
 * Specialization of {@link Result} for RxJava.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Experimental
public interface RxResult extends Result {
    /**
     * Returns the number of rows updated by a query against a database.  May be empty if the query did not update any rows.
     *
     * @return the number of rows updated by a query against a database
     * @throws IllegalStateException if the result was consumed
     */
    @Override
    Flowable<Integer> getRowsUpdated();

    /**
     * Returns a mapping of the rows that are the results of a query against a database.  May be empty if the query did not return any rows.  A {@link Row} can be only considered valid within a
     * {@link BiFunction mapping function} callback.
     *
     * @param mappingFunction the {@link BiFunction} that maps a {@link Row} and {@link RowMetadata} to a value
     * @return a mapping of the rows that are the results of a query against a database
     * @throws IllegalArgumentException if {@code mappingFunction} is {@code null}
     * @throws IllegalStateException    if the result was consumed
     */
    @Override
    <T> Flowable<T> map(BiFunction<Row, RowMetadata, ? extends T> mappingFunction);
}
