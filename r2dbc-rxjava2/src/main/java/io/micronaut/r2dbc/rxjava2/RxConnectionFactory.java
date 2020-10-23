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

import io.r2dbc.spi.ConnectionFactory;
import io.reactivex.Flowable;

import java.util.function.Function;

public interface RxConnectionFactory extends ConnectionFactory {
    /**
     * Creates a new {@link RxConnection}.
     *
     * @return the newly created {@link RxConnection}
     */
    @Override
    Flowable<? extends RxConnection> create();

    /**
     * Apply the given function closing the connection on termination.
     * @param publisherFunction The publisher function
     * @param <T> The return type
     * @return The flowable
     */
    <T> Flowable<T> withConnection(Function<RxConnection, Flowable<T>> publisherFunction);

    /**
     * Apply given function within a context of a transaction, rolling back if an error occurs and committing if not. This method will also close the connection on completion.
     *
     * @param publisherFunction The publisher function
     * @param <T> The return type
     * @return The flowable
     */
    <T> Flowable<T> withTransaction(Function<RxConnection, Flowable<T>> publisherFunction);
}
