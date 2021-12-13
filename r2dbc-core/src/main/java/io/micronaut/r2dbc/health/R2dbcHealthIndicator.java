/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.r2dbc.health;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Supports ony Postgres, MariaDB, MySQL
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.0.1
 */
@Requires(classes = HealthIndicator.class)
@Requires(beans = ConnectionFactory.class)
@Requires(condition = R2dbcHealthCondition.class)
@Requires(property = HealthEndpoint.PREFIX + ".r2dbc.enabled", value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
@Singleton
public class R2dbcHealthIndicator implements HealthIndicator {

    private static final String NAME = "r2dbc-connection-factory";
    private static final String DETAILS_METADATA = "metadata";

    private final ConnectionFactory connectionFactory;
    private final String healthQuery;

    @Inject
    public R2dbcHealthIndicator(ConnectionFactory connectionFactory,
                                R2dbcHealthQueryProvider queryProvider) {
        this.connectionFactory = connectionFactory;
        this.healthQuery = queryProvider.getHealthQuery(connectionFactory.getMetadata().getName())
                .orElseThrow(() -> new ConfigurationException("Unexpected behavior while getting Health Query for: " + connectionFactory));
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Mono.usingWhen(connectionFactory.create(),
                        connection -> Mono.from(connection.createStatement(healthQuery).execute())
                                .flatMapMany(result -> result.map(this::extractQueryResult))
                                .next(),
                        Connection::close, (o, throwable) -> o.close(), Connection::close)
                .map(this::buildUpResult)
                .onErrorResume(e -> Mono.just(buildDownResult(e)));
    }

    protected String extractQueryResult(Row row, RowMetadata metadata) {
        return String.valueOf(row.get(0));
    }

    private HealthResult buildUpResult(String metadata) {
        return HealthResult.builder(NAME)
                .status(HealthStatus.UP)
                .details(Collections.singletonMap(DETAILS_METADATA, metadata))
                .build();
    }

    private HealthResult buildDownResult(Throwable throwable) {
        return HealthResult.builder(NAME)
                .status(HealthStatus.DOWN)
                .exception(throwable)
                .build();
    }
}
