package example;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresServer {

    static PostgreSQLContainer<?> start() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag(PostgreSQLContainer.DEFAULT_TAG));
        container.start();
        return container;
    }
}
