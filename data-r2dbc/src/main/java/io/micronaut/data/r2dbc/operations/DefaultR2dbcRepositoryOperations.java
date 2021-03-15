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
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.attr.AttributeHolder;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.jdbc.operations.AbstractSqlRepositoryOperations;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.query.builder.sql.Dialect;
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
import io.micronaut.transaction.annotation.TransactionalAdvice;
import io.micronaut.transaction.exceptions.NoTransactionException;
import io.micronaut.transaction.exceptions.TransactionSystemException;
import io.micronaut.transaction.exceptions.TransactionUsageException;
import io.micronaut.transaction.interceptor.DefaultTransactionAttribute;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.micronaut.transaction.reactive.ReactiveTransactionStatus;
import io.r2dbc.spi.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.Duration;
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
public class DefaultR2dbcRepositoryOperations extends AbstractSqlRepositoryOperations<Row, Statement> implements R2dbcRepositoryOperations, R2dbcOperations, ReactiveTransactionOperations<Connection> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultR2dbcRepositoryOperations.class);
    private final ConnectionFactory connectionFactory;
    private final DefaultR2dbcReactiveRepositoryOperations reactiveOperations;
    private final boolean closeConnectionOnComplete;
    private final boolean isMariaDB;
    private final String dataSourceName;

    /**
     * Default constructor.
     *
     * @param dataSourceName        The data source name
     * @param connectionFactory     The associated connection factory
     * @param mediaTypeCodecList    The media type codec list
     * @param dateTimeProvider      The date time provider
     * @param runtimeEntityRegistry The runtime entity registry
     * @param applicationContext    The bean context
     */
    @Internal
    protected DefaultR2dbcRepositoryOperations(
            @Parameter String dataSourceName,
            ConnectionFactory connectionFactory,
            List<MediaTypeCodec> mediaTypeCodecList,
            @NonNull DateTimeProvider<Object> dateTimeProvider,
            RuntimeEntityRegistry runtimeEntityRegistry,
            ApplicationContext applicationContext) {
        super(
                dataSourceName,
                new ColumnNameR2dbcResultReader(),
                new ColumnIndexR2dbcResultReader(),
                new R2dbcQueryStatement(),
                mediaTypeCodecList,
                dateTimeProvider,
                runtimeEntityRegistry,
                applicationContext
        );
        this.connectionFactory = connectionFactory;
        this.reactiveOperations = new DefaultR2dbcReactiveRepositoryOperations();
        ConnectionFactoryMetadata metadata = connectionFactory.getMetadata();
        this.closeConnectionOnComplete = metadata.getName().equalsIgnoreCase("H2");
        this.isMariaDB = metadata.getName().equalsIgnoreCase("MariaDB");
        this.dataSourceName = dataSourceName;
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
    public <T> Iterable<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
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
    public <T> int delete(@NonNull DeleteOperation<T> operation) {
        final Number v = reactiveOperations.delete(operation).block();
        if (v != null) {
            return v.intValue();
        } else {
            return 0;
        }
    }

    @Override
    public <T> Optional<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
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
            public <T> CompletionStage<Number> delete(@NonNull DeleteOperation<T> operation) {
                return toCompletionStage(reactiveOperations.delete(operation));
            }

            @NonNull
            @Override
            public <T> CompletionStage<Iterable<T>> persistAll(@NonNull InsertBatchOperation<T> operation) {
                return toIterableCompletionStage(reactiveOperations.persistAll(operation));
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
            public <T> CompletionStage<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a new Connection for DataSource: " + dataSourceName);
        }
        if (closeConnectionOnComplete) {
            return Flux.usingWhen(connectionFactory.create(), handler, Mono::just);
        } else {
            return Flux.usingWhen(connectionFactory.create(), handler, (connection -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Closing Connection for DataSource: " + dataSourceName);
                }
                return connection.close();
            }));
        }
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

    @NonNull
    @Override
    public <T> Publisher<T> withTransaction(
            @NonNull ReactiveTransactionStatus<Connection> status,
            @NonNull ReactiveTransactionOperations.TransactionalCallback<Connection, T> handler) {
        Objects.requireNonNull(status, "Transaction status cannot be null");
        Objects.requireNonNull(handler, "Callback handler cannot be null");
        return Flux.defer(() -> {
            try {
                return handler.doInTransaction(status);
            } catch (Exception e) {
                return Flux.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
            }
        }).contextWrite(context -> context.put(ReactiveTransactionStatus.STATUS, status));
    }

    @Override
    public @NonNull
    <T> Flux<T> withTransaction(
            @NonNull TransactionDefinition definition,
            @NonNull ReactiveTransactionOperations.TransactionalCallback<Connection, T> handler) {
        Objects.requireNonNull(definition, "Transaction definition cannot be null");
        Objects.requireNonNull(handler, "Callback handler cannot be null");

        return Flux.deferContextual(contextView -> {
            Object o = !contextView.isEmpty() ? contextView.get(ReactiveTransactionStatus.STATUS) : null;
            TransactionDefinition.Propagation propagationBehavior = definition.getPropagationBehavior();
            if (o instanceof ReactiveTransactionStatus) {
                // existing transaction, use it
                if (propagationBehavior == TransactionDefinition.Propagation.NOT_SUPPORTED || propagationBehavior == TransactionDefinition.Propagation.NEVER) {
                    return Flux.error(new TransactionUsageException("Found an existing transaction but propagation behaviour doesn't support it: " + propagationBehavior));
                }
                try {
                    return handler.doInTransaction(((ReactiveTransactionStatus<Connection>) o));
                } catch (Exception e) {
                    return Flux.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
                }
            } else {

                if (propagationBehavior == TransactionDefinition.Propagation.MANDATORY) {
                    return Flux.error(new NoTransactionException("Expected an existing transaction, but none was found in the Reactive context."));
                }
                // in R2DBC the connection is automatically closed after transaction commit or rollback so a new one is used
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating a new Connection for DataSource: " + dataSourceName);
                }
                return Flux.from(connectionFactory.create()).flatMap(connection -> {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Transaction Begin for DataSource: {}", dataSourceName);
                            }
                            DefaultReactiveTransactionStatus status = new DefaultReactiveTransactionStatus(connection, true);
                            Mono<Boolean> resourceSupplier;
                            if (definition.getIsolationLevel() != TransactionDefinition.DEFAULT.getIsolationLevel()) {
                                IsolationLevel isolationLevel = getIsolationLevel(definition);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Setting Isolation Level ({}) for Transaction on DataSource: {}", isolationLevel, dataSourceName);
                                }
                                if (isolationLevel != null) {
                                    resourceSupplier = Mono.from(connection.setTransactionIsolationLevel(isolationLevel))
                                            .thenMany(connection.beginTransaction())
                                            .hasElements();
                                } else {
                                    resourceSupplier = Mono.from(connection.beginTransaction()).hasElement();
                                }
                            } else {
                                resourceSupplier = Mono.from(connection.beginTransaction()).hasElement();
                            }

                            return Flux.usingWhen(resourceSupplier,
                                    (b) -> {
                                        try {
                                            return Flux.from(handler.doInTransaction(status)).contextWrite(context ->
                                                    context.put(ReactiveTransactionStatus.STATUS, status)
                                                            .put(ReactiveTransactionStatus.ATTRIBUTE, definition)
                                            );
                                        } catch (Exception e) {
                                            return Mono.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
                                        }
                                    },
                                    (b) -> doCommit(status),
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
                                                }).doFinally((sig) -> status.completed = true);

                                    },
                                    (b) -> doCommit(status));
                        }
                );
            }
        });

    }

    private Publisher<Void> doCommit(DefaultReactiveTransactionStatus status) {
        if (status.isRollbackOnly()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rolling back transaction on DataSource {}.", dataSourceName);
            }
            return Mono.from(status.getConnection().rollbackTransaction()).doFinally(sig -> status.completed = true);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing transaction for DataSource {}.", dataSourceName);
            }
            return Mono.from(status.getConnection().commitTransaction()).doFinally(sig -> status.completed = true);
        }
    }

    /**
     * Represents the current reactive transaction status.
     */
    private static final class DefaultReactiveTransactionStatus implements ReactiveTransactionStatus<Connection> {
        private final Connection connection;
        private final boolean isNew;
        private boolean rollbackOnly;
        private boolean completed;

        public DefaultReactiveTransactionStatus(Connection connection, boolean isNew) {
            this.connection = connection;
            this.isNew = isNew;
        }

        @Override
        public Connection getConnection() {
            return connection;
        }

        @Override
        public boolean isNewTransaction() {
            return isNew;
        }

        @Override
        public void setRollbackOnly() {
            this.rollbackOnly = true;
        }

        @Override
        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }
    }

    /**
     * reactive operations implementation.
     */
    private final class DefaultR2dbcReactiveRepositoryOperations implements ReactiveRepositoryOperations {

        @Override
        public <T> Mono<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
            return Mono.from(withNewOrExistingTransaction(preparedQuery, false, status -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> status.getConnection().createStatement(sql),
                        preparedQuery,
                        false,
                        true
                );
                return Mono.from(statement.execute())
                        .flatMap((r) ->
                                Mono.from(r.map((row, metadata) -> true))
                        ).defaultIfEmpty(false);
            }));
        }

        @NonNull
        @Override
        public <T, R> Mono<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
            return Mono.from(withNewOrExistingTransaction(preparedQuery, false, status -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> status.getConnection().createStatement(sql),
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
                                            jsonCodec,
                                            (loadedEntity, o) -> {
                                                if (loadedEntity.hasPostLoadEventListeners()) {
                                                    return triggerPostLoad(o, loadedEntity, preparedQuery.getAnnotationMetadata());
                                                } else {
                                                    return o;
                                                }
                                            }
                                    );
                                    return mapper.map(row, resultType);
                                }))
                        );
            }));
        }

        @NonNull
        @Override
        public <T, R> Flux<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
            return Flux.from(withNewOrExistingTransaction(preparedQuery, false, status -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> status.getConnection().createStatement(sql),
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
                                                    jsonCodec,
                                                    (loadedEntity, o) -> {
                                                        if (loadedEntity.hasPostLoadEventListeners()) {
                                                            return triggerPostLoad(o, loadedEntity, preparedQuery.getAnnotationMetadata());
                                                        } else {
                                                            return o;
                                                        }
                                                    }
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
            }));
        }

        @NonNull
        @Override
        public Mono<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
            return Mono.from(withNewOrExistingTransaction(preparedQuery, true, status -> {
                @SuppressWarnings("Convert2MethodRef") Statement statement = prepareStatement(
                        (sql) -> status.getConnection().createStatement(sql),
                        preparedQuery,
                        true,
                        true
                );
                return Mono.from(statement.execute())
                        .flatMap((result) -> Mono.from(result.getRowsUpdated()).map(c -> {
                            Argument<?> argument = preparedQuery.getResultArgument().getFirstTypeVariable().orElse(null);
                            if (argument != null) {
                                if (argument.getType().isInstance(c)) {
                                    return c;
                                } else {
                                    return (Number) columnIndexResultSetReader.convertRequired(
                                            c,
                                            argument
                                    );
                                }
                            }
                            return c;
                        })).switchIfEmpty(Mono.fromCallable(() -> {
                            Argument<?> argument = preparedQuery.getResultArgument().getFirstTypeVariable().orElse(null);
                            return (Number) columnIndexResultSetReader.convertRequired(
                                    0L,
                                    argument
                            );
                        }));
            }));
        }

        @NonNull
        @Override
        public Mono<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery) {
            return executeUpdate(preparedQuery);
        }

        @NonNull
        @Override
        public <T> Mono<Number> delete(@NonNull DeleteOperation<T> operation) {
            final AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
            final String query = annotationMetadata.stringValue(Query.class)
                    .orElseThrow(() -> new DataAccessException("Query metadata missing from repository method. Consider recompiling the repository implementation."));
            final T entity;
            final RuntimePersistentEntity<T> persistentEntity =
                    (RuntimePersistentEntity<T>) getEntity(operation.getEntity().getClass());
            if (persistentEntity.hasPreRemoveEventListeners()) {
                entity = triggerPreRemove(operation.getEntity(), persistentEntity, annotationMetadata);
                if (entity == null) {
                    return Mono.just(0);
                }
            } else {
                entity = operation.getEntity();
            }
            return Mono.from(withNewOrExistingTransaction(operation, status -> {
                Statement statement = status.getConnection().createStatement(query);
                        final RuntimePersistentProperty<Object> idProperty = getIdReader(entity);
                        final Object id = idProperty.getProperty().get(entity);
                        if (id == null) {
                            throw new IllegalArgumentException("Passed entity has null ID: " + entity);
                        }
                        preparedStatementWriter.setDynamic(
                                statement,
                                0,
                                idProperty.getDataType(),
                                id
                        );

                        return Mono.from(statement.execute())
                                .flatMap((result -> Mono.from(result.getRowsUpdated()).map((num) -> {
                            if (num > 0) {
                                triggerPostRemove(entity, persistentEntity, annotationMetadata);
                            }
                            return (long) num;
                        }).defaultIfEmpty(0L)
                    )
                );
            })
            );
        }

        @NonNull
        @Override
        public <T> Flux<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
            StoredInsert<T> insert = resolveInsert(operation);
            if (insert.doesSupportBatch()) {

                return Flux.from(withNewOrExistingTransaction(operation, status -> {
                    List<T> results = new ArrayList<>(10);
                    boolean generateId = insert.isGenerateId();
                    String insertSql = insert.getSql();
                    BeanProperty<T, Object> identity = insert.getIdentityProperty();
                    RuntimePersistentProperty<?> identityProperty = insert.getIdentity();
                    final boolean hasGeneratedID = generateId && identity != null &&
                            insert.getDialect() != Dialect.ORACLE; // Oracle doesn't support batch inserts with returning IDs
                    final RuntimePersistentEntity<T> persistentEntity = insert.getPersistentEntity();

                    if (QUERY_LOG.isDebugEnabled()) {
                        QUERY_LOG.debug("Executing SQL Insert: {}", insertSql);
                    }
                    Statement statement = status.getConnection().createStatement(insertSql);
                    if (hasGeneratedID) {
                        statement.returnGeneratedValues(identityProperty.getPersistedName());
                    }

                    final boolean hasPrePersistEventListeners = persistentEntity.hasPrePersistEventListeners();
                    final AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
                    for (T entity : operation) {
                        if (hasPrePersistEventListeners) {
                            entity = triggerPrePersist(entity, persistentEntity, annotationMetadata);
                            if (entity == null) {
                                continue;
                            }
                        } else if (identity.hasSetterOrConstructorArgument()) {
                            entity = identity.withValue(entity, id);
                        }
                        setInsertParameters(insert, entity, statement);
                        statement.add();
                        results.add(entity);
                    }

                    Iterator<T> i = results.iterator();
                    final boolean hasPostPersistEventListeners = persistentEntity.hasPostPersistEventListeners();
                    final Flux<T> resultEmitter = Flux.from(statement.execute())
                            .flatMap(result -> result.map((row, metadata) -> {
                                T entity = i.next();
                                if (hasGeneratedID) {
                                    Object id = columnIndexResultSetReader.readDynamic(row, 0, identityProperty.getDataType());
                                    if (!identity.isReadOnly()) {
                                        if (identity.getType().isInstance(id)) {
                                            identity.set(entity, id);
                                        } else {
                                            identity.convertAndSet(entity, id);
                                        }
                                    }
                                }
                                if (hasPostPersistEventListeners) {
                                    triggerPostPersist(entity, persistentEntity, annotationMetadata);
                                }
                                return entity;
                            }));
                    if (!hasGeneratedID) {
                        return resultEmitter
                                .switchIfEmpty(Flux.fromIterable(results))
                                .map((entity) -> {
                                    if (hasPostPersistEventListeners) {
                                        triggerPostPersist(entity, persistentEntity, annotationMetadata);
                                    }
                                    return entity;
                                });
                    } else {
                        return resultEmitter;
                    }
                }));
            } else {
                return Flux.fromIterable(operation.split())
                        .flatMap(this::persist);
            }
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
            return Mono.from(withNewOrExistingTransaction(operation, status -> {
                boolean generateId = insert.isGenerateId();
                String insertSql = insert.getSql();
                BeanProperty<T, Object> identity = insert.getIdentityProperty();
                RuntimePersistentProperty<?> identityProperty = insert.getIdentity();
                final boolean hasGeneratedID = generateId && identity != null;
                final RuntimePersistentEntity<T> pe = insert.getPersistentEntity();
                T entity = operation.getEntity();
                T resolvedEntity;
                if (pe.hasPrePersistEventListeners()) {
                    final T newEntity = triggerPrePersist(entity, pe, operation.getAnnotationMetadata());
                    if (newEntity == null) {
                        // operation evicted
                        return Mono.empty();
                    } else {
                        resolvedEntity = newEntity;
                    }
                } else {
                    resolvedEntity = entity;
                }

                if (QUERY_LOG.isDebugEnabled()) {
                    QUERY_LOG.debug("Executing SQL Insert: {}", insertSql);
                }
                Statement statement = status.getConnection().createStatement(insertSql);
                setInsertParameters(insert, resolvedEntity, statement);
                if (hasGeneratedID) {
                    statement.returnGeneratedValues(identityProperty.getPersistedName());
                }
                return Mono.from(statement.execute()).flatMap((result) -> {
                    if (hasGeneratedID) {
                        return Mono.from(result.map((row, metadata) -> {
                            Object id = columnIndexResultSetReader.readDynamic(
                                    row,
                                    0,
                                    identityProperty.getDataType()
                            );
                            T finalEntity = resolvedEntity;
                            if (!identity.isReadOnly()) {
                                if (identity.getType().isInstance(id)) {
                                    identity.set(resolvedEntity, id);
                                } else {
                                    identity.convertAndSet(resolvedEntity, id);
                                }
                            } else if (identity.hasSetterOrConstructorArgument()) {
                                finalEntity = identity.withValue(resolvedEntity, id);
                            }

                            if (pe.hasPostPersistEventListeners()) {
                                finalEntity = triggerPostPersist(resolvedEntity, insert.getPersistentEntity(), insert.getIdentityProperty().getAnnotationMetadata());
                            }
                            return finalEntity;
                        }));
                    } else {
                        T finalEntity = resolvedEntity;
                        if (pe.hasPostPersistEventListeners()) {
                            finalEntity = triggerPostPersist(resolvedEntity, insert.getPersistentEntity(), insert.getIdentityProperty().getAnnotationMetadata());
                        }
                        return Mono.just(finalEntity);
                    }
                });
            }));
        }

        @NonNull
        @Override
        public <T> Mono<T> update(@NonNull UpdateOperation<T> operation) {
            final AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
            final String[] params = annotationMetadata.stringValues(DataMethod.class, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS);
            final String query = annotationMetadata.stringValue(Query.class).orElseThrow(() -> new DataAccessException("Query metadata missing from repository method. Consider recompiling the repository implementation."));
            final T entity;
            final RuntimePersistentEntity<T> persistentEntity =
                    (RuntimePersistentEntity<T>) getEntity(operation.getEntity().getClass());
            if (persistentEntity.hasPreUpdateEventListeners()) {
                entity = triggerPreUpdate(operation.getEntity(), persistentEntity, annotationMetadata);
                if (entity == null) {
                    return Mono.empty();
                }
            } else {
                entity = operation.getEntity();
            }
            return Mono.from(withNewOrExistingTransaction(operation, status -> {
                Statement statement = status.getConnection().createStatement(query);
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
                        .flatMap((num) -> {
                            if (num > 0) {
                                if (persistentEntity.hasPostUpdateEventListeners()) {
                                    triggerPostUpdate(entity, persistentEntity, annotationMetadata);

                                }
                                return Mono.just(entity);
                            } else {
                                return Mono.empty();
                            }
                        });
            }));
        }

        private @NonNull
        TransactionDefinition newTransactionDefinition(AttributeHolder attributeHolder) {
            return attributeHolder.getAttribute(ReactiveTransactionStatus.ATTRIBUTE, TransactionDefinition.class).orElseGet(() -> {
                if (attributeHolder instanceof AnnotationMetadataProvider) {

                    AnnotationValue<TransactionalAdvice> annotation = ((AnnotationMetadataProvider) attributeHolder)
                            .getAnnotationMetadata().getAnnotation(TransactionalAdvice.class);

                    if (annotation != null) {
                        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
                        attribute.setReadOnly(annotation.isTrue("readOnly"));
                        annotation.intValue("timeout").ifPresent(value -> attribute.setTimeout(Duration.ofSeconds(value)));
                        final Class[] noRollbackFors = annotation.classValues("noRollbackFor");
                        //noinspection unchecked
                        attribute.setNoRollbackFor(noRollbackFors);
                        annotation.enumValue("propagation", TransactionDefinition.Propagation.class)
                                .ifPresent(attribute::setPropagationBehavior);
                        annotation.enumValue("isolation", TransactionDefinition.Isolation.class)
                                .ifPresent(attribute::setIsolationLevel);
                        return attribute;
                    }
                }
                return TransactionDefinition.DEFAULT;
            });
        }

        private <T, R> Publisher<R> withNewOrExistingTransaction(
                @NonNull EntityOperation<T> operation,
                TransactionalCallback<Connection, R> entityOperation) {
            @SuppressWarnings("unchecked")
            ReactiveTransactionStatus<Connection> connection = operation
                    .getParameterInRole(R2dbcRepository.PARAMETER_TX_STATUS, ReactiveTransactionStatus.class).orElse(null);
            if (connection != null) {
                try {
                    return entityOperation.doInTransaction(connection);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            } else {
                return withNewOrExistingTxAttribute(operation, entityOperation, true);
            }
        }

        private <T, R> Publisher<R> withNewOrExistingTransaction(
                @NonNull PreparedQuery<T, R> operation,
                boolean isWrite,
                TransactionalCallback<Connection, R> entityOperation) {
            @SuppressWarnings("unchecked")
            ReactiveTransactionStatus<Connection> connection = operation
                    .getParameterInRole(R2dbcRepository.PARAMETER_TX_STATUS, ReactiveTransactionStatus.class).orElse(null);
            if (connection != null) {
                try {
                    return entityOperation.doInTransaction(connection);
                } catch (Exception e) {
                    return Mono.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
                }
            } else {
                return withNewOrExistingTxAttribute(operation, entityOperation, isWrite);
            }
        }

        private <T, R> Publisher<R> withNewOrExistingTxAttribute(
                @NonNull AttributeHolder attributeHolder,
                TransactionalCallback<Connection, R> entityOperation,
                boolean isWrite) {
            @SuppressWarnings("unchecked") ReactiveTransactionStatus<Connection> status =
                    attributeHolder.getAttribute(ReactiveTransactionStatus.STATUS, ReactiveTransactionStatus.class).orElse(null);
            if (status != null) {
                try {
                    return entityOperation.doInTransaction(status);
                } catch (Exception e) {
                    return Mono.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
                }
            } else {
                if (isWrite) {
                    TransactionDefinition definition = newTransactionDefinition(attributeHolder);
                    if (definition.isReadOnly()) {
                        return Mono.error(new TransactionUsageException("Cannot perform write operation with read-only transaction"));
                    } else {
                        return withTransaction(definition, entityOperation);
                    }
                } else {
                    return withConnection((c -> {
                        try {
                            return entityOperation.doInTransaction(new DefaultReactiveTransactionStatus(c, true));
                        } catch (Exception e) {
                            return Mono.error(new TransactionSystemException("Error invoking doInTransaction handler: " + e.getMessage(), e));
                        }
                    }));
                }
            }
        }

        @NonNull
        @Override
        public <T> Mono<Number> deleteAll(DeleteBatchOperation<T> operation) {
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
