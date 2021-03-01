package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.runtime.config.SchemaGenerate

trait H2TestPropertyProvider implements DatabaseTestPropertyProvider {

    @Override
    String url() {
        "r2dbc:h2:mem:///testdb;DB_CLOSE_ON_EXIT=FALSE"
    }

    @Override
    String username() {
        ""
    }

    @Override
    String password() {
        ""
    }

    @Override
    Dialect dialect() {
        Dialect.H2
    }

    @Override
    SchemaGenerate schemaGenerate() {
        SchemaGenerate.CREATE_DROP
    }
}
