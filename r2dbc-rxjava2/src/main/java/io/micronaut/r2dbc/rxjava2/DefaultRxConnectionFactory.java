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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
final class DefaultRxConnectionFactory implements RxConnectionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRxConnectionFactory.class);
    private final ConnectionFactory connectionFactory;
    private final boolean closeOnComplete;

    public DefaultRxConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.closeOnComplete = !connectionFactory.getMetadata().getName().equalsIgnoreCase("h2");
    }

    /**
     * Creates a new {@link RxConnection}.
     *
     * @return the newly created {@link RxConnection}
     */
    @Override
    public Flowable<? extends RxConnection> create() {
        Publisher<? extends Connection> publisher = connectionFactory.create();
        return Flowable.fromPublisher(publisher)
                .map((Function<Connection, RxConnection>) DefaultRxConnection::new);
    }

    @Override
    public <T> Flowable<T> withTransaction(RxConnectionFunction<T> publisherFunction) {
        return create()
                .switchMap(connection ->
                    connection.beginTransaction()
                            .isEmpty()
                            .flatMapPublisher(inTransaction -> (Publishers.MicronautPublisher<T>) actual -> {
                                Flowable<T> publisher = publisherFunction.apply(connection);
                                publisher.subscribe(new CompletionAwareSubscriber<T>() {
                                    @Override
                                    protected void doOnSubscribe(Subscription subscription) {
                                        actual.onSubscribe(subscription);
                                    }

                                    @Override
                                    protected void doOnNext(T message) {
                                        try {
                                            actual.onNext(message);
                                        } catch (Exception e) {
                                            onError(e);
                                        }
                                    }

                                    @Override
                                    protected void doOnError(Throwable t) {
                                        Flowable<Void> rollbackOp = connection.rollbackTransaction();
                                        if (closeOnComplete) {
                                            rollbackOp = rollbackOp.switchIfEmpty(connection.close());
                                        }
                                        //noinspection ResultOfMethodCallIgnored
                                        rollbackOp.isEmpty()
                                                .subscribe((v) -> actual.onError(t), (rollbackError) -> {
                                            if (LOG.isErrorEnabled()) {
                                                LOG.error("Error during R2DBC transaction rollback: " + rollbackError.getMessage(), rollbackError);
                                            }
                                            actual.onError(rollbackError);
                                        });
                                    }

                                    @Override
                                    protected void doOnComplete() {
                                        Flowable<Void> commitOp = connection
                                                .commitTransaction();
                                        if (closeOnComplete) {
                                            commitOp = commitOp.switchIfEmpty(connection.close());
                                        }
                                        //noinspection ResultOfMethodCallIgnored
                                        commitOp
                                                .isEmpty()
                                                .subscribe((v) -> actual.onComplete(), (commitError) -> {
                                            if (LOG.isErrorEnabled()) {
                                                LOG.error("Error during R2DBC transaction commit: " + commitError.getMessage(), commitError);
                                            }
                                            actual.onError(commitError);
                                        });
                                    }
                                });
                            })
                );
    }

    @Override
    public <T> Flowable<T> withConnection(RxConnectionFunction<T> publisherFunction) {
        return create()
                .switchMap(connection -> Flowable.fromPublisher((Publishers.MicronautPublisher<T>) actual -> {
                    Flowable<T> publisher = publisherFunction.apply(connection);
                    publisher.subscribe(new CompletionAwareSubscriber<T>() {
                        @Override
                        protected void doOnSubscribe(Subscription subscription) {
                            actual.onSubscribe(subscription);
                        }

                        @Override
                        protected void doOnNext(T message) {
                            try {
                                actual.onNext(message);
                            } catch (Exception e) {
                                onError(e);
                            }
                        }

                        @Override
                        protected void doOnError(Throwable t) {
                            if (closeOnComplete) {
                                //noinspection ResultOfMethodCallIgnored
                                connection.close().isEmpty().subscribe((v) -> actual.onError(t), (closeError) -> {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Error closing R2DBC connection: " + closeError.getMessage(), closeError);
                                    }
                                    actual.onError(t);
                                });
                            } else {
                                actual.onError(t);
                            }
                        }

                        @Override
                        protected void doOnComplete() {
                            if (closeOnComplete) {
                                connection.close().isEmpty().subscribe((v) -> actual.onComplete(), (closeError) -> {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Error closing R2DBC connection: " + closeError.getMessage(), closeError);
                                    }
                                    actual.onComplete();
                                });
                            } else {
                                actual.onComplete();
                            }

                        }
                    });
                }));
    }

    /**
     * Returns the {@link ConnectionFactoryMetadata} about the product this {@link ConnectionFactory} is applicable to.
     *
     * @return the {@link ConnectionFactoryMetadata} about the product this {@link ConnectionFactory} is applicable to
     */
    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return connectionFactory.getMetadata();
    }
}
