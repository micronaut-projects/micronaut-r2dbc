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

import io.micronaut.core.annotation.Internal;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.reactivex.Flowable;

import java.util.function.BiFunction;

@Internal
final class DefaultRxResult implements RxResult {
    private final Result result;

    public DefaultRxResult(Result result) {
        this.result = result;
    }

    @Override
    public Flowable<Integer> getRowsUpdated() {
        return Flowable.fromPublisher(result.getRowsUpdated());
    }

    @Override
    public <T> Flowable<T> map(BiFunction<Row, RowMetadata, ? extends T> mappingFunction) {
        return Flowable.fromPublisher(result.map(mappingFunction));
    }
}
