/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.r2dbc;

import io.micronaut.context.event.BeanPreDestroyEvent;
import io.micronaut.context.event.BeanPreDestroyEventListener;
import io.micronaut.core.annotation.Internal;
import io.r2dbc.spi.Closeable;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Closes the {@link ConnectionFactory} beans that {@link R2dbcConnectionFactoryBean} creates when they are destroyed,
 * for example when the application context stops.
 *
 * <p>A connection factory is closed only if it is closeable: an r2dbc-pool {@code ConnectionPool} implements both
 * {@link Closeable} and {@link Disposable}. A plain driver connection factory is left alone. Connection factories
 * created by other beans are left to whoever created them.</p>
 *
 * @author graemerocher
 * @since 7.2.1
 */
@Singleton
@Internal
final class ConnectionFactoryCloser implements BeanPreDestroyEventListener<ConnectionFactory> {

    /**
     * How long to wait for a connection factory to close.
     */
    static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(30);

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactoryCloser.class);

    @Override
    public ConnectionFactory onPreDestroy(BeanPreDestroyEvent<ConnectionFactory> event) {
        ConnectionFactory connectionFactory = event.getBean();
        boolean createdByModule = event.getBeanDefinition().getDeclaringType()
            .filter(R2dbcConnectionFactoryBean.class::isAssignableFrom)
            .isPresent();
        if (createdByModule) {
            close(connectionFactory);
        }
        return connectionFactory;
    }

    private static void close(ConnectionFactory connectionFactory) {
        try {
            if (connectionFactory instanceof Closeable closeable) {
                Mono.from(closeable.close()).block(CLOSE_TIMEOUT);
            } else if (connectionFactory instanceof Disposable disposable) {
                disposable.dispose();
            }
        } catch (RuntimeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Error closing R2DBC connection factory {}: {}", connectionFactory, e.getMessage(), e);
            }
        }
    }
}
