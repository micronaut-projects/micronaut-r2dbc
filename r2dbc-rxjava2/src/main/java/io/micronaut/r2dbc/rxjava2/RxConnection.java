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
package io.micronaut.r2dbc.rxjava2;

import io.micronaut.core.annotation.Experimental;
import io.r2dbc.spi.*;
import io.reactivex.Flowable;

@Experimental
public interface RxConnection extends Connection {
    /**
     * Creates a new {@link RxBatch} instance for building a batched request.
     *
     * @return a new {@link RxBatch} instance
     */
    @Override
    RxBatch createBatch();

    /**
     * Creates a new statement for building a statement-based request.
     *
     * @param sql the SQL of the statement
     * @return a new {@link RxStatement} instance
     * @throws IllegalArgumentException if {@code sql} is {@code null}
     */
    @Override
    RxStatement createStatement(String sql);

    /**
     * Begins a new transaction.  Calling this method disables {@link #isAutoCommit() auto-commit} mode.
     *
     * @return a {@link Flowable} that indicates that the transaction is open
     */
    @Override
    Flowable<Void> beginTransaction();

    /**
     * Release any resources held by the {@link Connection}.
     *
     * @return a {@link Flowable} that termination is complete
     */
    @Override
    Flowable<Void> close();

    /**
     * Commits the current transaction.
     *
     * @return a {@link Flowable} that indicates that a transaction has been committed
     */
    @Override
    Flowable<Void> commitTransaction();

    /**
     * Creates a savepoint in the current transaction.
     *
     * @param name the name of the savepoint to create
     * @return a {@link Flowable} that indicates that a savepoint has been created
     * @throws IllegalArgumentException      if {@code name} is {@code null}
     * @throws UnsupportedOperationException if savepoints are not supported
     */
    @Override
    Flowable<Void> createSavepoint(String name);

    /**
     * Releases a savepoint in the current transaction.  Calling this for drivers not supporting savepoint release results in a no-op.
     *
     * @param name the name of the savepoint to release
     * @return a {@link Flowable} that indicates that a savepoint has been released
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    @Override
    Flowable<Void> releaseSavepoint(String name);

    /**
     * Rolls back the current transaction.
     *
     * @return a {@link Flowable} that indicates that a transaction has been rolled back
     */
    @Override
    Flowable<Void> rollbackTransaction();

    /**
     * Rolls back to a savepoint in the current transaction.
     *
     * @param name the name of the savepoint to rollback to
     * @return a {@link Flowable} that indicates that a savepoint has been rolled back to
     * @throws IllegalArgumentException      if {@code name} is {@code null}
     * @throws UnsupportedOperationException if savepoints are not supported
     */
    @Override
    Flowable<Void> rollbackTransactionToSavepoint(String name);

    /**
     * Configures the auto-commit mode for the current transaction.
     * If a connection is in auto-commit mode, then all {@link Statement}s will be executed and committed as individual transactions.
     * Otherwise, in explicit transaction mode, transactions have to be {@link #beginTransaction() started} explicitly.
     * A transaction needs to be either {@link #commitTransaction() committed} or {@link #rollbackTransaction() rolled back} to clean up the transaction state.
     * <p>
     * Calling this method during an active transaction and the
     * auto-commit mode is changed, the transaction is committed.  Calling this method without changing auto-commit mode this invocation results in a no-op.
     *
     * @param autoCommit the isolation level for this transaction
     * @return a {@link Flowable} that indicates that auto-commit mode has been configured
     */
    @Override
    Flowable<Void> setAutoCommit(boolean autoCommit);

    /**
     * Configures the isolation level for the current transaction.
     * <p>Isolation level is typically one of the following constants:
     *
     * <ul>
     * <li>{@link IsolationLevel#READ_UNCOMMITTED}</li>
     * <li>{@link IsolationLevel#READ_COMMITTED}</li>
     * <li>{@link IsolationLevel#REPEATABLE_READ}</li>
     * <li>{@link IsolationLevel#SERIALIZABLE}</li>
     * </ul>
     * <p>
     * {@link IsolationLevel} is extensible so drivers can accept a vendor-specific {@link IsolationLevel}.
     *
     * @param isolationLevel the isolation level for this transaction
     * @return a {@link Flowable} that indicates that a transaction level has been configured
     * @throws IllegalArgumentException if {@code isolationLevel} is {@code null}
     */
    @Override
    Flowable<Void> setTransactionIsolationLevel(IsolationLevel isolationLevel);

    /**
     * Validates the connection according to the given {@link ValidationDepth}.
     * Emits {@literal true} if the validation was successful or {@literal false} if the validation failed. Does not emit errors and does not complete empty.
     *
     * @param depth the validation depth
     * @return a {@link Flowable} that indicates whether the validation was successful
     * @throws IllegalArgumentException if {@code depth} is {@code null}
     */
    @Override
    Flowable<Boolean> validate(ValidationDepth depth);
}
