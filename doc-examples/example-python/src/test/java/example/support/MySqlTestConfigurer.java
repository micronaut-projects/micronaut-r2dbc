package example.support;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Starts the MySQL test container and supplies its JDBC (Flyway) and R2DBC datasource properties to
 * the tests run with the {@code mysql} environment, like the {@code TestPropertyProvider} of the Java,
 * Kotlin and Groovy example tests does.
 * <p>
 * The configurer is written in Java because Micronaut Test calls {@code TestPropertyProvider} before
 * the application context, and with it the GraalPy runtime, exists, so a Python test class cannot
 * supply the container properties. It uses the {@link #configure(ApplicationContext)} callback because
 * the {@link io.micronaut.context.ApplicationContextBuilder} is configured before {@code @MicronautTest}
 * selects the environments, so the {@code mysql} environment can only be checked on the built context.
 */
@ContextConfigurer
public class MySqlTestConfigurer implements ApplicationContextConfigurer {

    public static final String MYSQL_ENVIRONMENT = "mysql";
    public static final String MYSQL_IMAGE = "mysql:8.4.5";

    private static MySQLContainer<?> container;

    @Override
    public void configure(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (environment.getActiveNames().contains(MYSQL_ENVIRONMENT)) {
            environment.addPropertySource(PropertySource.of(MYSQL_ENVIRONMENT, getProperties()));
        }
    }

    private static synchronized Map<String, Object> getProperties() {
        if (container == null) {
            container = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE));
            container.start();
        }
        return Map.ofEntries(
            Map.entry("datasources.default.url", container.getJdbcUrl()),
            Map.entry("datasources.default.username", container.getUsername()),
            Map.entry("datasources.default.password", container.getPassword()),
            Map.entry("datasources.default.database", container.getDatabaseName()),
            Map.entry("datasources.default.driverClassName", container.getDriverClassName()),
            Map.entry("r2dbc.datasources.default.host", container.getHost()),
            Map.entry("r2dbc.datasources.default.port", container.getFirstMappedPort()),
            Map.entry("r2dbc.datasources.default.driver", "mysql"),
            Map.entry("r2dbc.datasources.default.username", container.getUsername()),
            Map.entry("r2dbc.datasources.default.password", container.getPassword()),
            Map.entry("r2dbc.datasources.default.database", container.getDatabaseName()),
            Map.entry("r2dbc.datasources.default.options.tlsVersion", "TLSv1.2")
        );
    }
}
