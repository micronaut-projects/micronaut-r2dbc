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
package io.micronaut.r2dbc.mssql;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.r2dbc.AbstractBasicR2dbcProperties;
import io.micronaut.r2dbc.BasicR2dbcProperties;
import io.r2dbc.spi.Option;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;

/**
 * Implementation of {@link BasicR2dbcProperties} for MSSQL.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@EachProperty(value = BasicR2dbcProperties.PREFIX, primary = "default")
public class MssqlConnectionFactoryConfiguration extends AbstractBasicR2dbcProperties<String> {

        /**
         * Default constructor.
         *
         * @param name        The name of the datasource
         * @param environment The environment
         */
    protected MssqlConnectionFactoryConfiguration(@Parameter String name, Environment environment) {
        super(name, newConnectionFactoryOptionsBuilder(name, environment, null));
    }

        /**
         * Sets the connection properties.
         *
         * @param options The options
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setOptions(
            @MapFormat(keyFormat = StringConvention.RAW)
            @Nullable Map<String, String> options) {
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
         *
         * @param protocol The protocol
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setProtocol(@NotBlank String protocol) {
        return (MssqlConnectionFactoryConfiguration) super.setProtocol(protocol);
    }

        /**
         * Sets the connect timeout.
         *
         * @param duration The duration
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setConnectTimeout(Duration duration) {
        return (MssqlConnectionFactoryConfiguration) super.setConnectTimeout(duration);
    }

        /**
         * Sets whether to prefer SSL configuration
         *
         * @param ssl Sets whether to prefer SSL
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setSsl(boolean ssl) {
        return (MssqlConnectionFactoryConfiguration) super.setSsl(ssl);
    }

        /**
         * Sets the host.
         *
         * @param host The host
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setHost(@NotBlank String host) {
        return (MssqlConnectionFactoryConfiguration) super.setHost(host);
    }

        /**
         * Sets the port.
         *
         * @param port The port
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setPort(@Positive int port) {
        return (MssqlConnectionFactoryConfiguration) super.setPort(port);
    }

        /**
         * Sets the username.
         *
         * @param username The username
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setUsername(@NotBlank String username) {
        return (MssqlConnectionFactoryConfiguration) super.setUsername(username);
    }

        /**
         * Sets the password.
         *
         * @param password The password
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setPassword(CharSequence password) {
        return (MssqlConnectionFactoryConfiguration) super.setPassword(password);
    }

        /**
         * Sets the initial database name.
         *
         * @param database The database
         * @return These properties
         */
        @Override
        public MssqlConnectionFactoryConfiguration setDatabase(@NotBlank String database) {
        return (MssqlConnectionFactoryConfiguration) super.setDatabase(database);
    }
    }
