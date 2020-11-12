/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.r2dbc.config;

import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.r2dbc.spi.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schema generation for R2DBC.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Context
@Internal
public class R2dbcSchemaGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(R2dbcSchemaGenerator.class);
    private final List<DataR2dbcConfiguration> configurations;

    /**
     * Default constructor.
     * @param configurations The configurations.
     */
    public R2dbcSchemaGenerator(List<DataR2dbcConfiguration> configurations) {
        this.configurations = configurations;
    }

    /**
     * Creates the schema.
     */
    @PostConstruct
    protected void createSchema() {
        for (DataR2dbcConfiguration configuration : configurations) {

            SchemaGenerate schemaGenerate = configuration.getSchemaGenerate();
            if (schemaGenerate != null && schemaGenerate != SchemaGenerate.NONE) {
                String name = configuration.getName();
                List<String> packages = configuration.getPackages();

                Collection<BeanIntrospection<Object>> introspections;
                if (CollectionUtils.isNotEmpty(packages)) {
                    introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class, packages.toArray(new String[0]));
                } else {
                    introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class);
                }
                PersistentEntity[] entities = introspections.stream()
                        // filter out inner / internal / abstract(MappedSuperClass) classes
                        .filter(i -> !i.getBeanType().getName().contains("$"))
                        .filter(i -> !java.lang.reflect.Modifier.isAbstract(i.getBeanType().getModifiers()))
                        .map(PersistentEntity::of).toArray(PersistentEntity[]::new);
                if (ArrayUtils.isNotEmpty(entities)) {
                    R2dbcOperations r2dbcOperations = configuration.getR2dbcOperations();
                    SqlQueryBuilder builder = new SqlQueryBuilder(configuration.getDialect());

                    Mono.from(r2dbcOperations.withConnection(connection -> {
                        List<String> createStatements = Arrays.stream(entities)
                                .flatMap(entity -> Arrays.stream(builder.buildCreateTableStatements(entity)))
                                .collect(Collectors.toList());
                        Flux<? extends Result> createTablesFlow = Flux.fromIterable(createStatements)
                                .flatMap(sql -> {
                                    if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                        DataSettings.QUERY_LOG.debug("Creating Table: \n{}", sql);
                                    }
                                    return Mono.from(connection.createStatement(sql).execute())
                                            .onErrorResume((throwable -> {
                                                if (LOG.isWarnEnabled()) {
                                                    LOG.warn("Unable to create table :{}", throwable.getMessage());
                                                }
                                                return Mono.empty();
                                            }));
                                });
                        switch (schemaGenerate) {
                            case CREATE_DROP:
                                List<String> dropStatements = Arrays.stream(entities).flatMap(entity -> Arrays.stream(builder.buildDropTableStatements(entity)))
                                                                    .collect(Collectors.toList());
                                return Flux.fromIterable(dropStatements)
                                        .flatMap(sql -> {
                                            if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                                DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
                                            }
                                            return Mono.from(connection.createStatement(sql).execute())
                                                    .onErrorResume((throwable -> Mono.empty()));
                                        })
                                        .thenMany(createTablesFlow)
                                        .then();
                            case CREATE:
                            default:
                                return createTablesFlow
                                        .then();
                        }

                    })).block();

                }
            }
        }
    }
}
