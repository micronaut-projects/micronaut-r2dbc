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
package io.micronaut.data.r2dbc.operations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.jdbc.operations.AbstractSqlRepositoryOperations;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.*;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.r2dbc.mapper.ColumnIndexR2dbcResultReader;
import io.micronaut.data.r2dbc.mapper.ColumnNameR2dbcResultReader;
import io.micronaut.data.r2dbc.mapper.R2dbcQueryStatement;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.data.runtime.mapper.TypeMapper;
import io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.transaction.TransactionDefinition;
import io.r2dbc.spi.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Defines an implementation of Micronaut Data's core interfaces for R2DBC.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@EachBean(ConnectionFactory.class)
public class DefaultR2dbcRepositoryOperations extends AbstractSqlRepositoryOperations<Row, Statement> implements R2dbcRepositoryOperations, R2dbcOperations {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultR2dbcRepositoryOperations.class);
    private final ConnectionFactory connectionFactory;
    private final DefaultR2dbcReactiveRepositoryOperations reactiveOperations;
    private final boolean closeConnectionOnComplete;

    /**
     * Default constructor.
     *
     * @param dataSourceName               The data source name
     * @param connectionFactory  The associated connection factory
     * @param mediaTypeCodecList The media type codec list
     * @param dateTimeProvider   The date time provider
     * @param beanContext        The bean context
     */
    protected DefaultR2dbcRepositoryOperations(
            @Parameter String dataSourceName,
            ConnectionFactory connectionFactory,
            List<MediaTypeCodec> mediaTypeCodecList,
            @NonNull DateTimeProvider<?> dateTimeProvider,
            BeanContext beanContext) {
        super(
                dataSourceName,
                new ColumnNameR2dbcResultReader(),
                new ColumnIndexR2dbcResultReader(),
                new R2dbcQueryStatement(),
                mediaTypeCodecList,
                dateTimeProvider,
                beanContext
        );
        this.connectionFactory = connectionFactory;
        this.reactiveOperations = new DefaultR2dbcReactiveRepositoryOperations();
        ConnectionFactoryMetadata metadata = connectionFactory.getMetadata();
        this.closeConnectionOnComplete = metadata.getName().equalsIgnoreCase("H2");
    }

    @Override
    protected int shiftIndex(int i) {
        return i;
    }

    @NonNull
    @Override
    public ReactiveRepositoryOperations reactive() {
        return reactiveOperations;
    }

    @Nullable
    @Override
    public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
        return reactiveOperations.findOne(preparedQuery).block();
    }

    @NonNull
    @Override
    public <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
        return reactiveOperations.findAll(preparedQuery)
                .collectList().block();
    }

    @NonNull
    @Override
    public <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery) {
        return reactiveOperations.findAll(preparedQuery)
                .collectList().block().stream();
    }

    @NonNull
    @Override
    public <T> T persist(@NonNull InsertOperation<T> operation) {
        return reactiveOperations.persist(operation).block();
    }

    @NonNull
    @Override
    public <T> T update(@NonNull UpdateOperation<T> operation) {
        return reactiveOperations.update(operation).block();
    }

    @NonNull
    @Override
    public <T> Iterable<T> persistAll(@NonNull BatchOperation<T> operation) {
        return reactiveOperations.persistAll(operation)
                .collectList().block();
    }

    @NonNull
    @Override
    public Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
        Number number = reactiveOperations.executeUpdate(preparedQuery).block();
        return Optional.ofNullable(number);
    }

    @Override
    public <T> Optional<Number> deleteAll(@NonNull BatchOperation<T> operation) {
        Number result = reactiveOperations.deleteAll(operation).block();
        return Optional.ofNullable(result);
    }

    @Override
    public <T> boolean exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
        return reactiveOperations.exists(preparedQuery).block();
    }

    @Override
    public <R> Page<R> findPage(@NonNull PagedQuery<R> query) {
        throw new UnsupportedOperationException("The findPage method without an explicit query is not supported. Use findPage(PreparedQuery) instead");
    }

    @Nullable
    @Override
    public <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id) {
        throw new UnsupportedOperationException("The findAll method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
    }

    @NonNull
    @Override
    public <T> Iterable<T> findAll(@NonNull PagedQuery<T> query) {
        throw new UnsupportedOperationException("The findAll method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
    }

    @Override
    public <T> long count(PagedQuery<T> pagedQuery) {
        throw new UnsupportedOperationException("The count method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
    }

    @NonNull
    @Override
    public <T> Stream<T> findStream(@NonNull PagedQuery<T> query) {
        throw new UnsupportedOperationException("The findStream method without an explicit query is not supported. Use findStream(PreparedQuery) instead");

    }

    @NonNull
    @Override
    public AsyncRepositoryOperations async() {
        return new AsyncRepositoryOperations() {
            @NonNull
            @Override
            public Executor getExecutor() {
                throw new UnsupportedOperationException("R2DBC implementation doesn't support direct access to executor service");
            }

            @NonNull
            @Override
            public <T> CompletionStage<T> findOne(@NonNull Class<T> type, @NonNull Serializable id) {
                return toCompletionStage(reactiveOperations.findOne(type, id));
            }

            @Override
            public <T> CompletionStage<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
                return toCompletionStage(reactiveOperations.exists(preparedQuery));
            }

            @NonNull
            @Override
            public <T, R> CompletionStage<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
                return toCompletionStage(reactiveOperations.findOne(preparedQuery));
            }

            @NonNull
            @Override
            public <T> CompletionStage<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id) {
                return toCompletionStage(reactiveOperations.findOptional(type, id));
            }

            @NonNull
            @Override
            public <T, R> CompletionStage<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery) {
                return toCompletionStage(reactiveOperations.findOptional(preparedQuery));
            }

            @NonNull
            @Override
            public <T> CompletionStage<Iterable<T>> findAll(PagedQuery<T> pagedQuery) {
                return toIterableCompletionStage(reactiveOperations.findAll(pagedQuery));
            }

            @NonNull
            @Override
            public <T> CompletionStage<Long> count(PagedQuery<T> pagedQuery) {
                return toCompletionStage(reactiveOperations.count(pagedQuery));
            }

            @NonNull
            @Override
            public <T, R> CompletionStage<Iterable<R>> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
                return toIterableCompletionStage(reactiveOperations.findAll(preparedQuery));
            }

            @NonNull
            @Override
            public <T> CompletionStage<T> persist(@NonNull InsertOperation<T> operation) {
                return toCompletionStage(reactiveOperations.persist(operation));
            }

            @NonNull
            @Override
            public <T> CompletionStage<T> update(@NonNull UpdateOperation<T> operation) {
                return toCompletionStage(reactiveOperations.update(operation));
            }

            @NonNull
            @Override
            public <T> CompletionStage<Iterable<T>> persistAll(@NonNull BatchOperation<T> operation) {
                return toIterableCompletionStage(
                        reactiveOperations.persistAll(operation)
                );
            }

            private <T> CompletionStage<Iterable<T>> toIterableCompletionStage(Flux<T> flux) {
                CompletableFuture<Iterable<T>> cs = new CompletableFuture<>();
                flux.collectList().subscribe(
                        cs::complete,
                        cs::completeExceptionally
                );
                return cs;
            }

            @NonNull
            @Override
            public CompletionStage<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
                return toCompletionStage(reactiveOperations.executeUpdate(preparedQuery));
            }

            @NonNull
            @Override
            public <T> CompletionStage<Number> deleteAll(@NonNull BatchOperation<T> operation) {
                return toCompletionStage(reactiveOperations.deleteAll(operation));
            }

            @NonNull
            @Override
            public <R> CompletionStage<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery) {
                return toCompletionStage(reactiveOperations.findPage(pagedQuery));
            }

            private <T> CompletionStage<T> toCompletionStage(Mono<T> publisher) {
                CompletableFuture<T> cs = new CompletableFuture<>();
                publisher.subscribe(
                        cs::complete,
                        cs::completeExceptionally
                );
                return cs;
            }
        };
    }

    @NonNull
    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @NonNull
    @Override
    public <T> Publisher<T> withConnection(@NonNull Function<Connection, Publisher<T>> handler) {
        Objects.requireNonNull(handler, "Handler cannot be null");
        if (closeConnectionOnComplete) {
            return Flux.usingWhen(connectionFactory.create(), handler, Mono::just);
        } else {
            return Flux.usingWhen(connectionFactory.create(), handler, Connection::close);
        }
    }

    @NonNull
    @Override
    public <T> Publisher<T> withTransaction(@NonNull TransactionDefinition definition, @NonNull Function<Connection, Publisher<T>> handler) {
        Objects.requireNonNull(definition, "Transaction definition cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");

        return reactiveOperations.withNewTransactionMany(definition, handler.andThen(Flux::from));
    }

    private IsolationLevel getIsolationLevel(TransactionDefinition definition) {
        TransactionDefinition.Isolation isolationLevel = definition.getIsolationLevel();
        switch (isolationLevel) {
            case READ_COMMITTED:
                return IsolationLevel.READ_COMMITTED;
            case READ_UNCOMMITTED:
                return IsolationLevel.READ_UNCOMMITTED;
            case REPEATABLE_READ:
                return IsolationLevel.REPEATABLE_READ;
            case SERIALIZABLE:
                return IsolationLevel.SERIALIZABLE;
            default:
                return null;
        }
    }

    /**
     * reactive operations implementation.
     */
    private final class DefaultR2dbcReactiveRepositoryOperations implements ReactiveRepositoryOperations {

        @Override
        public <T> Mono<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
            return withNewOrExistingConnection(preparedQuery, connection -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> connection.createStatement(sql),
                        preparedQuery,
                        false,
                        true
                );
                return Mono.from(statement.execute())
                        .flatMap((r) ->
                                Mono.from(r.map((row, metadata) -> true))
                        ).defaultIfEmpty(false);
            });
        }

        @NonNull
        @Override
        public <T, R> Mono<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
            return withNewOrExistingConnection(preparedQuery, connection -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> connection.createStatement(sql),
                        preparedQuery,
                        false,
                        true
                );
                return Mono.from(statement.execute())
                        .flatMap((r) ->
                                Mono.from(r.map((row, metadata) -> {
                                    Class<R> resultType = preparedQuery.getResultType();
                                    RuntimePersistentEntity<R> persistentEntity = getEntity(resultType);
                                    TypeMapper<Row, R> mapper = new SqlResultEntityTypeMapper<>(
                                            persistentEntity,
                                            columnNameResultSetReader,
                                            preparedQuery.getJoinFetchPaths(),
                                            jsonCodec
                                    );
                                    return mapper.map(row, resultType);
                                }))
                        );
            });
        }

        @NonNull
        @Override
        public <T, R> Flux<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
            return withNewOrExistingConnectionMany(preparedQuery, connection -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> connection.createStatement(sql),
                        preparedQuery,
                        false,
                        false
                );
                return Flux.from(statement.execute())
                        .flatMap((r) -> {
                                    Publisher<Optional<R>> mapped = r.map((row, metadata) -> {
                                        Class<R> resultType = preparedQuery.getResultType();
                                        boolean dtoProjection = preparedQuery.isDtoProjection();
                                        boolean isEntity = preparedQuery.getResultDataType() == DataType.ENTITY;
                                        if (isEntity || dtoProjection) {
                                            RuntimePersistentEntity<R> persistentEntity = getEntity(resultType);
                                            TypeMapper<Row, R> mapper = new SqlResultEntityTypeMapper<>(
                                                    persistentEntity,
                                                    columnNameResultSetReader,
                                                    preparedQuery.getJoinFetchPaths(),
                                                    jsonCodec
                                            );
                                            return Optional.of(mapper.map(row, resultType));
                                        } else {
                                            Object v = columnIndexResultSetReader
                                                    .readDynamic(row, 0, preparedQuery.getResultDataType());
                                            if (v == null) {
                                                return Optional.empty();
                                            } else if (resultType.isInstance(v)) {
                                                return Optional.of((R) v);
                                            } else {
                                                Object converted = columnIndexResultSetReader.convertRequired(v, resultType);
                                                if (converted != null) {
                                                    return Optional.of((R) converted);
                                                } else {
                                                    return Optional.empty();
                                                }
                                            }
                                        }
                                    });
                                    return Flux.from(mapped).flatMap(opt -> opt.<Flux<? extends R>>map(Flux::just).orElseGet(Flux::empty));
                                }
                        );
            });
        }

        @NonNull
        @Override
        public Mono<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
            return withNewOrExistingTransaction(preparedQuery, connection -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> connection.createStatement(sql),
                        preparedQuery,
                        true,
                        true
                );
                return Mono.from(statement.execute())
                        .flatMap((result) -> Mono.from(result.getRowsUpdated()));
            });
        }

        @NonNull
        @Override
        public <T> Flux<T> persistAll(@NonNull BatchOperation<T> operation) {
            StoredInsert<T> insert = resolveInsert(operation);
            return withNewOrExistingTransactionMany(operation, connection -> {
                List<T> results = new ArrayList<>(10);
                boolean generateId = insert.isGenerateId();
                String insertSql = insert.getSql();
                BeanProperty<T, Object> identity = insert.getIdentityProperty();
                RuntimePersistentProperty<?> identityProperty = insert.getIdentity();
                final boolean hasGeneratedID = generateId && identity != null;

                if (QUERY_LOG.isDebugEnabled()) {
                    QUERY_LOG.debug("Executing SQL Insert: {}", insertSql);
                }
                Statement statement = connection.createStatement(insertSql);
                if (hasGeneratedID) {
                    statement.returnGeneratedValues(identityProperty.getPersistedName());
                }
                for (T entity : operation) {
                    setInsertParameters(insert, entity, statement);
                    statement.add();
                    results.add(entity);
                }

                Iterator<T> i = results.iterator();
                return Flux.from(statement.execute()).flatMap(result -> result.map((row, metadata) -> {
                    T entity = i.next();
                    if (hasGeneratedID) {
                        Object id = columnIndexResultSetReader.readDynamic(row, 0, identityProperty.getDataType());
                        if (!identity.isReadOnly()) {
                            identity.set(entity, id);
                        }
                    }
                    return entity;
                }));
            });
        }

        @NonNull
        @Override
        public <T, R> Mono<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery) {
            return findOne(preparedQuery);
        }

        @NonNull
        @Override
        public <T> Mono<T> persist(@NonNull InsertOperation<T> operation) {
            StoredInsert<T> insert = resolveInsert(operation);
            return withNewOrExistingTransaction(operation, connection -> {
                boolean generateId = insert.isGenerateId();
                String insertSql = insert.getSql();
                BeanProperty<T, Object> identity = insert.getIdentityProperty();
                RuntimePersistentProperty<?> identityProperty = insert.getIdentity();
                final boolean hasGeneratedID = generateId && identity != null;

                if (QUERY_LOG.isDebugEnabled()) {
                    QUERY_LOG.debug("Executing SQL Insert: {}", insertSql);
                }
                Statement statement = connection.createStatement(insertSql);

                T entity = operation.getEntity();
                setInsertParameters(insert, entity, statement);
                if (hasGeneratedID) {
                    statement.returnGeneratedValues(identityProperty.getPersistedName());
                }
                return Mono.from(statement.execute()).flatMap((result) -> {
                    if (hasGeneratedID) {
                        return Mono.from(result.map((row, metadata) -> {

                            Object id = columnIndexResultSetReader.readDynamic(row, 0, identityProperty.getDataType());
                            if (!identity.isReadOnly()) {
                                identity.set(entity, id);
                            }
                            return entity;
                        }));
                    } else {
                        return Mono.just(entity);
                    }
                });
            });
        }

        @NonNull
        @Override
        public <T> Mono<T> update(@NonNull UpdateOperation<T> operation) {
            final AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
            final String[] params = annotationMetadata.stringValues(DataMethod.class, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS);
            final String query = annotationMetadata.stringValue(Query.class).orElse(null);
            final T entity = operation.getEntity();
            final RuntimePersistentEntity<T> persistentEntity =
                    (RuntimePersistentEntity<T>) getEntity(entity.getClass());
            return withNewOrExistingTransaction(operation, connection -> {
                Statement statement = connection.createStatement(query);
                for (int i = 0; i < params.length; i++) {
                    String param = params[i];
                    RuntimePersistentProperty<T> pp = persistentEntity.getPropertyByName(param);
                    if (pp != null) {
                        BeanProperty<T, ?> bp = pp.getProperty();
                        preparedStatementWriter.setDynamic(
                                statement,
                                i,
                                pp.getDataType(),
                                bp.get(entity)
                        );
                    }
                }
                return Mono.from(statement.execute())
                           .flatMap((result -> Mono.from(result.getRowsUpdated())))
                           .flatMap((num) -> num > 0 ? Mono.just(entity) : Mono.empty());
            });
        }

        private <T> Mono<T> withNewOrExistingTransaction(@NonNull EntityOperation<T> operation, Function<Connection, Mono<T>> entityOperation) {
            Connection connection = operation.getParameterInRole(R2dbcRepository.PARAMETER_CONNECTION, Connection.class).orElse(null);
            if (connection != null) {
                return entityOperation.apply(connection);
            } else {
                return withNewTransaction(entityOperation);
            }
        }

        private <T, R> Mono<R> withNewOrExistingTransaction(@NonNull PreparedQuery<T, R> operation, Function<Connection, Mono<R>> entityOperation) {
            Connection connection = operation.getParameterInRole(R2dbcRepository.PARAMETER_CONNECTION, Connection.class).orElse(null);
            if (connection != null) {
                return entityOperation.apply(connection);
            } else {
                return withNewTransaction(entityOperation);
            }
        }

        private <T, R> Mono<R> withNewOrExistingConnection(@NonNull PreparedQuery<T, R> operation, Function<Connection, Mono<R>> entityOperation) {
            Connection connection = operation.getParameterInRole(R2dbcRepository.PARAMETER_CONNECTION, Connection.class).orElse(null);
            if (connection != null) {
                return entityOperation.apply(connection);
            } else {
                return withNewConnection(entityOperation);
            }
        }

        private <T, R> Flux<R> withNewOrExistingConnectionMany(@NonNull PreparedQuery<T, R> operation, Function<Connection, Flux<R>> entityOperation) {
            Connection connection = operation.getParameterInRole(R2dbcRepository.PARAMETER_CONNECTION, Connection.class).orElse(null);
            if (connection != null) {
                return entityOperation.apply(connection);
            } else {
                return withNewConnectionMany(entityOperation);
            }
        }

        private <T> Flux<T> withNewOrExistingTransactionMany(@NonNull EntityOperation<T> operation, Function<Connection, Flux<T>> entityOperation) {
            Connection connection = operation.getParameterInRole(R2dbcRepository.PARAMETER_CONNECTION, Connection.class).orElse(null);
            if (connection != null) {
                return entityOperation.apply(connection);
            } else {
                return withNewTransactionMany(null, entityOperation);
            }
        }

        @NonNull
        private <R> Mono<R> withNewConnection(Function<Connection, Mono<R>> handler) {
            ConnectionFactoryMetadata metadata = connectionFactory.getMetadata();
            if (metadata.getName().equalsIgnoreCase("H2")) {
                return Mono.usingWhen(connectionFactory.create(), handler, Mono::just);
            } else {
                return Mono.usingWhen(connectionFactory.create(), handler, (Connection::close));
            }
        }

        private <R> Flux<R> withNewConnectionMany(Function<Connection, Flux<R>> handler) {
            ConnectionFactoryMetadata metadata = connectionFactory.getMetadata();
            if (metadata.getName().equalsIgnoreCase("H2")) {
                return Flux.usingWhen(connectionFactory.create(), handler, Mono::just);
            } else {
                return Flux.usingWhen(connectionFactory.create(), handler, (Connection::close));
            }
        }

        private <R> Flux<R> withNewTransactionMany(@Nullable TransactionDefinition definition, Function<Connection, Flux<R>> handler) {
            // in R2DBC the connection is automatically closed after transaction commit or rollback so a new one is used
            return Flux.from(connectionFactory.create()).flatMap(connection -> {
                Mono<Boolean> resourceSupplier = Mono.from(connection.beginTransaction()).hasElement();
                if (definition != null && definition != TransactionDefinition.DEFAULT) {
                    IsolationLevel isolationLevel = getIsolationLevel(definition);
                    if (isolationLevel != null) {
                        resourceSupplier = resourceSupplier
                                .then(Mono.from(
                                        connection.setTransactionIsolationLevel(isolationLevel)).hasElement());
                    }
                }
                return Flux.usingWhen(resourceSupplier,
                                (b) -> handler.apply(connection),
                                (b) -> {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Committing transaction.");
                                    }
                                    return connection.commitTransaction();
                                },
                                (b, throwable) -> {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn("Rolling back transaction on error: " + throwable.getMessage(), throwable);
                                    }
                                    return Mono.from(connection.rollbackTransaction())
                                            .hasElement()
                                            .onErrorResume((rollbackError) -> {
                                                if (rollbackError != throwable && LOG.isWarnEnabled()) {
                                                    LOG.warn("Error occurred during transaction rollback: " + rollbackError.getMessage(), rollbackError);
                                                }
                                                return Mono.error(throwable);
                                            })
                                            .then(Mono.error(throwable));

                                },
                                (b) -> {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Transactional Operating cancelled.");
                                    }
                                    // seems like cancel should == rollback but reactor calls cancel() internally even for successful operations
                                    return connection.commitTransaction();
                                });
                    }
            );
        }

        private <R> Mono<R> withNewTransaction(Function<Connection, Mono<R>> handler) {
            // in R2DBC the connection is automatically closed after transaction commit or rollback so a new one is used
            return Mono.from(connectionFactory.create()).flatMap(connection -> Mono.usingWhen(Mono.from(connection.beginTransaction()).hasElement(),
                    (b) -> handler.apply(connection),
                    (b) -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Committing transaction.");
                        }
                        return connection.commitTransaction();
                    },
                    (b, throwable) -> {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Rolling back transaction on error: " + throwable.getMessage(), throwable);
                        }
                        return Mono.from(connection.rollbackTransaction())
                                    .hasElement()
                                    .onErrorResume((rollbackError) -> {
                                        if (rollbackError != throwable && LOG.isWarnEnabled()) {
                                            LOG.warn("Error occurred during transaction rollback: " + rollbackError.getMessage(), rollbackError);
                                        }
                                        return Mono.error(throwable);
                                    })
                                    .then(Mono.error(throwable));

                    },
                    (b) -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Transactional Operating cancelled.");
                        }
                        // seems like cancel should == rollback but reactor calls cancel() internally even for successful operations
                        return connection.commitTransaction();
                    })
            );
        }

        @NonNull
        @Override
        public <T> Mono<Number> deleteAll(BatchOperation<T> operation) {
            throw new UnsupportedOperationException("The deleteAll method is not supported. Execute the SQL query directly");
        }

        @NonNull
        @Override
        public <T> Mono<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id) {
            throw new UnsupportedOperationException("The findOptional method by ID is not supported. Execute the SQL query directly");
        }

        @NonNull
        @Override
        public <R> Mono<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery) {
            throw new UnsupportedOperationException("The findPage method is not supported. Execute the SQL query directly");
        }

        @NonNull
        @Override
        public <T> Mono<T> findOne(@NonNull Class<T> type, @NonNull Serializable id) {
            throw new UnsupportedOperationException("The findOne method by ID is not supported. Execute the SQL query directly");

        }

        @NonNull
        @Override
        public <T> Mono<Long> count(PagedQuery<T> pagedQuery) {
            throw new UnsupportedOperationException("The count method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
        }

        @NonNull
        @Override
        public <T> Flux<T> findAll(PagedQuery<T> pagedQuery) {
            throw new UnsupportedOperationException("The findAll method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
        }
    }
}
