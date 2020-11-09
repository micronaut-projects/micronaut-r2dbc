package io.micronaut.data.r2dbc;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import io.micronaut.data.tck.entities.Pet;
import reactor.core.publisher.Mono;

@R2dbcRepository(dialect = Dialect.H2)
interface H2PetRepository extends ReactiveStreamsCrudRepository<Pet, Long> {

    @Join("owner")
    Mono<Pet> findByName(String name);
}
