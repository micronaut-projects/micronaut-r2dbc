package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.entities.Person
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.data.tck.tests.AbstractReactiveRepositorySpec
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.reactivex.Single
import reactor.core.publisher.Mono
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb")
class H2ReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Inject
    @Shared
    H2ReactivePersonRepository personReactiveRepository

    @Inject
    @Shared
    ConnectionFactory connectionFactory

    @Inject
    @Shared
    R2dbcOperations r2dbcOperations

    @Override
    PersonReactiveRepository getPersonRepository() {
        return personReactiveRepository
    }

    void 'test with transactional connection'() {
        given:
        personReactiveRepository.save(new Person(name: "Tony")).blockingGet()

        when:
        Person person = Single.fromPublisher(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<java.sql.Connection> status ->
            personReactiveRepository.findByName("Tony", status).toFlowable()
        })).blockingGet()

        then:
        person != null
    }

    @Override
    void init() {
        Mono.from(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<Connection> status ->
            status.connection.createStatement(
                "CREATE TABLE `person` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(255) NOT NULL, `age` INT, `enabled` BIT);"
            ).execute()
        })).block()
    }
}
