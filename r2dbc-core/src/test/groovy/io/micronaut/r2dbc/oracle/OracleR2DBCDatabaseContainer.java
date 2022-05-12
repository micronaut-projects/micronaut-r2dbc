package io.micronaut.r2dbc.oracle;

import io.r2dbc.spi.ConnectionFactoryOptions;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.r2dbc.R2DBCDatabaseContainer;

import java.util.Set;

public class OracleR2DBCDatabaseContainer implements R2DBCDatabaseContainer {

    private final OracleContainer container;

    public OracleR2DBCDatabaseContainer(OracleContainer container) {
        this.container = container;
    }

    @Override
    public ConnectionFactoryOptions configure(ConnectionFactoryOptions options) {
        return options.mutate()
            .option(ConnectionFactoryOptions.HOST, container.getHost())
            .option(ConnectionFactoryOptions.PORT, container.getFirstMappedPort())
            .option(ConnectionFactoryOptions.DATABASE, container.getDatabaseName())
            .option(ConnectionFactoryOptions.USER, container.getUsername())
            .option(ConnectionFactoryOptions.PASSWORD, container.getPassword())
            .build();
    }

    @Override
    public Set<Startable> getDependencies() {
        return container.getDependencies();
    }

    @Override
    public void start() {
        container.start();
    }

    @Override
    public void stop() {
        container.start();
    }

    @Override
    public void close() {
        container.close();
    }
}
