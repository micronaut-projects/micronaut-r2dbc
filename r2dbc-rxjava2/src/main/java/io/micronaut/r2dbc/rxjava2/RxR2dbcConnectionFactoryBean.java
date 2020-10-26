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

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.r2dbc.BasicR2dbcProperties;
import io.micronaut.r2dbc.R2dbcConnectionFactoryBean;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

@Factory
@Internal
@Experimental
public class RxR2dbcConnectionFactoryBean extends R2dbcConnectionFactoryBean {
    @Override
    @EachBean(BasicR2dbcProperties.class)
    @Replaces(
            factory = R2dbcConnectionFactoryBean.class,
            bean = ConnectionFactoryOptions.Builder.class
    )
    protected ConnectionFactoryOptions.Builder connectionFactoryOptionsBuilder(BasicR2dbcProperties basicR2dbcProperties) {
        return super.connectionFactoryOptionsBuilder(basicR2dbcProperties);
    }

    @Override
    @EachBean(ConnectionFactoryOptions.Builder.class)
    @Replaces(
            factory = R2dbcConnectionFactoryBean.class,
            bean = ConnectionFactoryOptions.class
    )
    protected ConnectionFactoryOptions connectionFactoryOptions(ConnectionFactoryOptions.Builder builder) {
        return super.connectionFactoryOptions(builder);
    }

    /**
     * Method that exposes the {@link ConnectionFactory}.
     *
     * @param options the options
     * @return The connection factory
     */
    @Override
    @EachBean(ConnectionFactoryOptions.class)
    @Context
    @Replaces(
            factory = R2dbcConnectionFactoryBean.class,
            bean = ConnectionFactory.class
    )
    protected RxConnectionFactory connectionFactory(ConnectionFactoryOptions options) {
        ConnectionFactory connectionFactory = super.connectionFactory(options);
        return new DefaultRxConnectionFactory(connectionFactory);
    }
}
