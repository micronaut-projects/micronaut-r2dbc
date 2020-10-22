package io.micronaut.r2dbc;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * Core factory bean that exposes the following beans:
 *
 * <ul>
 *     <li>The {@link ConnectionFactoryOptions.Builder}</li>
 *     <li>The {@link ConnectionFactoryOptions}</li>
 *     <li>The {@link ConnectionFactory}</li>
 * </ul>
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Factory
public class R2dbcConnectionFactoryBean {

    /**
     * Method that exposes the {@link io.r2dbc.spi.ConnectionFactoryOptions.Builder}.
     * @param basicR2dbcProperties The basic properties
     * @return The builder
     */
    @EachBean(BasicR2dbcProperties.class)
    protected ConnectionFactoryOptions.Builder connectionFactoryOptionsBuilder(BasicR2dbcProperties<?> basicR2dbcProperties) {
        return basicR2dbcProperties.builder();
    }

    /**
     * Method that exposes the {@link ConnectionFactoryOptions}.
     * @param builder The builder
     * @return The options
     */
    @EachBean(ConnectionFactoryOptions.Builder.class)
    protected ConnectionFactoryOptions connectionFactoryOptions(ConnectionFactoryOptions.Builder builder) {
        return builder.build();
    }

    /**
     * Method that exposes the {@link ConnectionFactory}.
     * @param options the options
     * @return The connection factory
     */
    @EachBean(ConnectionFactoryOptions.class)
    @Context
    protected ConnectionFactory connectionFactory(ConnectionFactoryOptions options) {
        return ConnectionFactories.get(options);
    }
}
