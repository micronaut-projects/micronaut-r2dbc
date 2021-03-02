package io.micronaut.data.r2dbc

import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.r2dbc.spi.ConnectionFactory
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({env["GITHUB_WORKFLOW"]})
class PostgresReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Shared
    PostgresPersonRepository personReactiveRepository

    @Shared
    PostgresProductRepository productReactiveRepository

    @Shared
    ConnectionFactory connectionFactory

    @Shared
    R2dbcOperations r2dbcOperations

    @Shared
    @AutoCleanup
    ApplicationContext applicationContext

    @Shared
    @AutoCleanup
    PostgreSQLContainer container

    @Override
    PersonReactiveRepository getPersonRepository() {
        return personReactiveRepository
    }

    @Override
    ProductReactiveRepository getProductRepository() {
        return productReactiveRepository
    }

    @Override
    protected void init() {
        container = new PostgreSQLContainer("postgres:10")
        container.start()
        applicationContext = ApplicationContext.run(
                'r2dbc.datasources.default.url': "r2dbc:postgresql://localhost:${container.getFirstMappedPort()}/${container.getDatabaseName()}",
                'r2dbc.datasources.default.username': container.getUsername(),
                'r2dbc.datasources.default.password': container.getPassword(),
                "r2dbc.datasources.default.schema-generate":"CREATE_DROP",
                "r2dbc.datasources.default.dialect": "POSTGRES"
        )
        personReactiveRepository = applicationContext.getBean(PostgresPersonRepository)
        productReactiveRepository = applicationContext.getBean(PostgresProductRepository)
        connectionFactory = applicationContext.getBean(ConnectionFactory)
        r2dbcOperations = applicationContext.getBean(R2dbcOperations)
        super.init()
    }

}
