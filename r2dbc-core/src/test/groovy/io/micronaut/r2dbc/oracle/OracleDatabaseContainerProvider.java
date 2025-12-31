package io.micronaut.r2dbc.oracle;

import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.r2dbc.R2DBCDatabaseContainer;
import org.testcontainers.r2dbc.R2DBCDatabaseContainerProvider;
import org.testcontainers.utility.DockerImageName;

public class OracleDatabaseContainerProvider implements R2DBCDatabaseContainerProvider {

    static final String DRIVER = "oracle";

    @Override
    public boolean supports(ConnectionFactoryOptions options) {
        return DRIVER.equals(options.getRequiredValue(ConnectionFactoryOptions.DRIVER));
    }

    @Override
    public R2DBCDatabaseContainer createContainer(ConnectionFactoryOptions options) {
        OracleContainer container = new OracleContainer(DockerImageName.parse("gvenzl/oracle-free:slim-faststart")
            .asCompatibleSubstituteFor("gvenzl/oracle-free"))
            .withDatabaseName((String) options.getRequiredValue(ConnectionFactoryOptions.DATABASE));

        if (Boolean.TRUE.equals(options.getValue(REUSABLE_OPTION))) {
            container.withReuse(true);
        }
        return new OracleR2DBCDatabaseContainer(container);
    }

    @Override
    public ConnectionFactoryMetadata getMetadata(ConnectionFactoryOptions options) {
        ConnectionFactoryOptions.Builder builder = options.mutate();
        return R2DBCDatabaseContainerProvider.super.getMetadata(builder.build());
    }
}
