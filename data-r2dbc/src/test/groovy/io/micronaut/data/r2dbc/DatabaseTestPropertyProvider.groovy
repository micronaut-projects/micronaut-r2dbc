package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.runtime.config.SchemaGenerate
import io.micronaut.test.support.TestPropertyProvider

trait DatabaseTestPropertyProvider implements TestPropertyProvider {

    @Override
    Map<String, String> getProperties() {
        [
                "r2dbc.datasources.default.url"            : url(),
                "r2dbc.datasources.default.username"       : username(),
                "r2dbc.datasources.default.password"       : password(),
                "r2dbc.datasources.default.schema-generate": schemaGenerate(),
                "r2dbc.datasources.default.dialect"        : dialect()
        ] as Map<String, String>
    }

    abstract String url()

    abstract String username()

    abstract String password()

    abstract Dialect dialect()

    SchemaGenerate schemaGenerate() {
        SchemaGenerate.CREATE
    }
}

