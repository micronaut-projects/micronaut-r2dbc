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

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.r2dbc.spi.ConnectionFactory;

/**
 * Replacement connection factory for RxJava 2.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Factory
@Internal
@Experimental
public class RxR2dbcConnectionFactoryBean {

    /**
     * Method that exposes the {@link ConnectionFactory}.
     *
     * @param injectionPoint The injection point
     * @param beanContext The bean context
     * @return The connection factory
     */
    @Secondary
    @Bean
    protected RxConnectionFactory connectionFactory(@Nullable InjectionPoint<?> injectionPoint, BeanContext beanContext) {
        if (injectionPoint != null) {
            final String n = injectionPoint.getAnnotationMetadata().stringValue("javax.inject.Named").orElse(null);
            if (n != null) {
                return new DefaultRxConnectionFactory(beanContext.getBean(ConnectionFactory.class, Qualifiers.byName(n)));
            } else {
                return new DefaultRxConnectionFactory(beanContext.getBean(ConnectionFactory.class));
            }
        } else {
            return new DefaultRxConnectionFactory(beanContext.getBean(ConnectionFactory.class));
        }
    }
}
