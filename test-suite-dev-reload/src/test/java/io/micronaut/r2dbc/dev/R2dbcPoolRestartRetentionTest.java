/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.r2dbc.dev;

import io.micronaut.context.ApplicationContext;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Runs an application with an R2DBC connection pool ({@code r2dbc:pool:h2:mem:}) through the development runtime,
 * which restarts the application on a change. The connection factory is declared retainable, so the next generation
 * is served the same pool, still open, with no {@code micronaut.dev.retain} entry, and the first generation is
 * collected: neither the pool nor what it was created from holds anything of the context that created it.
 * Configuration under {@code r2dbc.datasources} releases the pool.
 */
class R2dbcPoolRestartRetentionTest {

    private static final String GREETER = "example.Greeter";

    @TempDir
    Path project;

    @BeforeAll
    static void initializeH2() throws ClassNotFoundException {
        // H2 preallocates an exception as it initializes TraceObject, whose stack trace would otherwise hold the
        // frames of the first generation that opens a connection, and with them its classes
        Class.forName("org.h2.message.TraceObject", true, R2dbcPoolRestartRetentionTest.class.getClassLoader());
    }

    @Test
    void thePoolIsRetainedAcrossARestart() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            database(harness, "r2dbc:pool:h2:mem:///" + UUID.randomUUID());
            harness.property("r2dbc.datasources.default.options.initial-size", "1");
            greeter(harness, "one");
            harness.start();
            ConnectionPool pool = pool(harness.context());
            // a table of the in-memory database lives as long as the pool keeps a connection to it
            execute(pool, "CREATE TABLE RETAINED (ID INT)");

            greeter(harness, "two");
            harness.reload();
            assertEquals(2, harness.generation());

            ReloadTck.assertRetained(harness, pool);
            assertSame(pool, pool(harness.context()));
            assertFalse(pool.isDisposed());
            assertEquals(0L, count(pool, "SELECT COUNT(*) FROM RETAINED"), "the database of the retained pool was kept");
            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void anR2dbcConfigurationChangeReleasesThePool() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            String url = "r2dbc:pool:h2:mem:///" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
            database(harness, url);
            greeter(harness, "one");
            harness.start();
            ConnectionPool first = pool(harness.context());
            execute(first, "CREATE TABLE RELEASED (ID INT)");

            // the application properties change under r2dbc.datasources, together with a class, so the application
            // restarts
            harness.resource("application.properties", """
                r2dbc.datasources.default.url=%s
                r2dbc.datasources.default.options.max-size=3
                """.formatted(url));
            greeter(harness, "two");
            harness.reload();
            assertEquals(2, harness.generation());

            ConnectionPool second = pool(harness.context());
            assertNotSame(first, second);
            assertEquals("3", String.valueOf(harness.context().getBean(io.r2dbc.spi.ConnectionFactoryOptions.class)
                .getValue(io.r2dbc.spi.Option.valueOf("maxSize"))));
            assertEquals(0L, count(second, "SELECT COUNT(*) FROM RELEASED"), "the new pool connects to the same database");
            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    private static void database(ReloadHarness harness, String url) {
        harness.property("r2dbc.datasources.default.url", url);
    }

    private static void greeter(ReloadHarness harness, String greeting) {
        harness.source(GREETER, """
            package example;

            @jakarta.inject.Singleton
            public class Greeter {
                private final io.r2dbc.spi.ConnectionFactory connectionFactory;

                public Greeter(io.r2dbc.spi.ConnectionFactory connectionFactory) {
                    this.connectionFactory = connectionFactory;
                }

                public String greet() {
                    return "%s";
                }
            }
            """.formatted(greeting));
    }

    private static ConnectionPool pool(ApplicationContext context) {
        return (ConnectionPool) context.getBean(ConnectionFactory.class);
    }

    private static void execute(ConnectionFactory connectionFactory, String sql) {
        Mono.usingWhen(connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute()).flatMap(result -> result.getRowsUpdated()).then(),
                Connection::close)
            .block();
    }

    private static long count(ConnectionFactory connectionFactory, String sql) {
        return Mono.usingWhen(connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                    .flatMap(result -> result.map((row, metadata) -> ((Number) row.get(0)).longValue()))
                    .single(),
                Connection::close)
            .block();
    }
}
