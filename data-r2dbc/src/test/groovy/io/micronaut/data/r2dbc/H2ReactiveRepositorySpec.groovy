package io.micronaut.data.r2dbc


import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.entities.Person
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.ConnectionFactory
import io.reactivex.Single
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@H2Properties
class H2ReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Inject
    @Shared
    H2ReactivePersonRepository personReactiveRepository

    @Inject
    @Shared
    H2ReactiveProductRepository productReactiveRepository

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

    @Override
    ProductReactiveRepository getProductRepository() {
        return productReactiveRepository
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
}
