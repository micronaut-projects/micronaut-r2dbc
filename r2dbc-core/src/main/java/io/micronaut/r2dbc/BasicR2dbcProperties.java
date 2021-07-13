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
import io.micronaut.core.naming.Named;
import io.r2dbc.spi.ConnectionFactoryOptions;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;

/**
 * An interface to model configuring basic R2DBC properties.
 *
 * @since 1.0.0
 * @author graemerocher
 */
public interface BasicR2dbcProperties extends Named {
    /**
     * The prefix for configuration.
     */
    String PREFIX = "r2dbc.datasources";

    /**
     * Sets the host.
     * @param host The host
     * @return These properties
     */
    BasicR2dbcProperties setHost(@NotBlank String host);

    /**
     * Sets the port.
     * @param port The port
     * @return These properties
     */
    BasicR2dbcProperties setPort(@Positive int port);

    /**
     * Sets the driver.
     * @param driver The driver
     * @return These properties
     */
    BasicR2dbcProperties setDriver(@NotBlank String driver);

    /**
     * Sets the protocol.
     * @param protocol The protocol
     * @return These properties
     */
    BasicR2dbcProperties setProtocol(@NotBlank String protocol);

    /**
     * Sets the connect timeout.
     * @param duration The duration
     * @return These properties
     */
    BasicR2dbcProperties setConnectTimeout(Duration duration);

    /**
     * Sets whether to prefer SSL configuration.
     * @param ssl Sets whether to prefer SSL
     * @return These properties
     */
    BasicR2dbcProperties setSsl(boolean ssl);

    /**
     * Sets the username.
     * @param username The username
     * @return These properties
     */
    BasicR2dbcProperties setUsername(@NotBlank String username);

    /**
     * Sets the password.
     * @param password The password
     * @return These properties
     */
    BasicR2dbcProperties setPassword(CharSequence password);

    /**
     * Sets the initial database name.
     * @param database The database
     * @return These properties
     */
    BasicR2dbcProperties setDatabase(@NotBlank String database);

    /**
     * Sets the connection properties.
     * @param options The options
     * @return These properties
     */
    BasicR2dbcProperties setOptions(
            @Nullable Map<String, String> options);

    /**
     * @return Return the current builder
     */
    @NonNull ConnectionFactoryOptions.Builder builder();
}
