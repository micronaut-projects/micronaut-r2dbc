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
import io.r2dbc.spi.Statement;
import io.reactivex.Flowable;


/**
 * Specialization of {@link Statement} for RxJava.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Experimental
public interface RxStatement extends Statement {
    /**
     * Executes one or more SQL statements and returns the {@link RxResult}s.
     * {@link RxResult} objects must be fully consumed to ensure full execution of the {@link RxStatement}.
     *
     * @return the {@link RxResult}s, returned by each statement
     * @throws IllegalStateException if the statement is parametrized and not all parameter values are provided
     */
    @Override
    Flowable<? extends RxResult> execute();

    @Override
    RxStatement returnGeneratedValues(String... columns);

    @Override
    RxStatement add();

    @Override
    RxStatement bind(int index, Object value);

    @Override
    RxStatement bind(String name, Object value);

    @Override
    RxStatement bindNull(int index, Class<?> type);

    @Override
    RxStatement bindNull(String name, Class<?> type);

    @Override
    RxStatement fetchSize(int rows);
}
