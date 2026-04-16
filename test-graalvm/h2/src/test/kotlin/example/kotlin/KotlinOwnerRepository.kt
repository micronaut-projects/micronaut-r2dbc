package example.kotlin

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Mono
import testgraalvm.domain.Owner

@R2dbcRepository(dialect = Dialect.H2)
interface KotlinOwnerRepository : ReactorCrudRepository<Owner, Long> {
    fun findByName(name: String): Mono<Owner>
}
