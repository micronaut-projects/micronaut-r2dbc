package io.micronaut.data.r2dbc

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rollback = false)
@H2Properties
class H2ImmutableTypeSpec extends Specification {

    @Inject ImmutablePetRepository repository

    void "test insert instance with nullable read-only values"() {
        when:
        def result = repository.save(new ImmutablePet(null, null)).block()

        then:
        result.id
    }
}
