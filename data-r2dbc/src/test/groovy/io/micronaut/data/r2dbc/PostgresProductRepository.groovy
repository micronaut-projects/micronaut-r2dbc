package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface PostgresProductRepository extends ProductReactiveRepository {
}
