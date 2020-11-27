package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository

@R2dbcRepository(dialect = Dialect.SQL_SERVER)
interface MssqlProductRepository extends ProductReactiveRepository {
}
