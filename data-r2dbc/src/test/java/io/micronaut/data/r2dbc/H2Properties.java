package io.micronaut.data.r2dbc;

import io.micronaut.context.annotation.Property;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "r2dbc.datasources.default.schema-generate", value = "CREATE_DROP")
@Property(name = "r2dbc.datasources.default.dialect", value = "H2")
@Retention(RetentionPolicy.RUNTIME)
public @interface H2Properties {
}
