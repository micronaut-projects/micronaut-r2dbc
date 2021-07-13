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
package io.micronaut.r2dbc;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;

/**
 * Abstract implementation of {@link BasicR2dbcProperties}.
 * @author graemerocher
 * @since 1.0.0
 */
@EachProperty(value = BasicR2dbcProperties.PREFIX, primary = "default")
public class DefaultBasicR2dbcProperties implements BasicR2dbcProperties {
    private final ConnectionFactoryOptions.Builder builder;
    private final String name;

    /**
     * Default constructor.
     * @param name The name of the datasource
     * @param environment The environment
     */
    protected DefaultBasicR2dbcProperties(@Parameter String name, Environment environment) {
        this.builder = newConnectionFactoryOptionsBuilder(name, environment, null);
        this.name = name;
    }

    @Override
    @NonNull public ConnectionFactoryOptions.Builder builder() {
        return builder;
    }

    /**
     * Sets the driver.
     *
     * @param driver The driver
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setDriver(@NotBlank String driver) {
        if (driver != null) {
            this.builder.option(ConnectionFactoryOptions.DRIVER, driver);
        }
        return this;
    }

    /**
     * Sets the connection properties.
     *
     * @param options The options
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setOptions(
            @MapFormat(keyFormat = StringConvention.RAW)
            @Nullable
            Map<String, String> options) {
        if (CollectionUtils.isNotEmpty(options)) {
            options.forEach((key, value) ->
                    getBuilder().option(
                            Option.valueOf(key),
                            value
                    )
            );
        }
        return this;

    }

    /**
     * Sets the protocol.
     * @param protocol The protocol
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setProtocol(@NotBlank String protocol) {
        if (protocol != null) {
            this.builder.option(ConnectionFactoryOptions.PROTOCOL, protocol);
        }
        return this;
    }

    /**
     * Sets the connect timeout.
     *
     * @param duration The duration
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setConnectTimeout(Duration duration) {
        if (duration != null) {
            this.builder.option(ConnectionFactoryOptions.CONNECT_TIMEOUT, duration);
        }
        return this;
    }

    /**
     * Sets whether to prefer SSL configuration.
     *
     * @param ssl Sets whether to prefer SSL
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setSsl(boolean ssl) {
        this.builder.option(ConnectionFactoryOptions.SSL, ssl);
        return this;
    }

    /**
     * Create a {@link ConnectionFactoryOptions.Builder} from the configured URL if present.
     * @param name The name of the datasource
     * @param env The environment
     * @param defaultUrl The default URL to use
     * @return The builder
     */
    protected static ConnectionFactoryOptions.Builder newConnectionFactoryOptionsBuilder(String name, Environment env, @Nullable String defaultUrl) {
        String property = PREFIX + "." + name + ".url";
        String url = env.getProperty(
                property, Argument.STRING
        ).orElse(defaultUrl);
        if (url != null) {
            return ConnectionFactoryOptions.parse(url).mutate();
        } else {
            return ConnectionFactoryOptions.builder();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The connection factory options builder.
     */
    public ConnectionFactoryOptions.Builder getBuilder() {
        return builder;
    }

    /**
     * Sets the host.
     * @param host The host
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setHost(@NotBlank String host) {
        builder.option(
                ConnectionFactoryOptions.HOST, host
        );
        return this;
    }

    /**
     * Sets the port.
     * @param port The port
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setPort(@Positive int port) {
        builder.option(
                ConnectionFactoryOptions.PORT, port
        );
        return this;
    }

    /**
     * Sets the username.
     * @param username The username
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setUsername(@NotBlank String username) {
        builder.option(
                ConnectionFactoryOptions.USER, username
        );
        return this;
    }

    /**
     * Sets the password.
     * @param password The password
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setPassword(CharSequence password) {
        builder.option(
                ConnectionFactoryOptions.PASSWORD, password
        );
        return this;
    }

    /**
     * Sets the initial database name.
     * @param database The database
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties setDatabase(@NotBlank String database) {
        builder.option(
                ConnectionFactoryOptions.DATABASE, database
        );
        return this;
    }
}
