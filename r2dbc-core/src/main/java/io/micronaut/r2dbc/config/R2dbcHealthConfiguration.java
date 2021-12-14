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
package io.micronaut.r2dbc.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.management.endpoint.health.HealthEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration for R2DBC Health Indicator.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.1.0
 * @see io.micronaut.r2dbc.health.R2dbcHealthIndicator
 */
@ConfigurationProperties(HealthEndpoint.PREFIX + ".r2dbc")
public class R2dbcHealthConfiguration {

    private boolean enabled = true;

    /**
     * {@link io.r2dbc.spi.ConnectionFactoryMetadata#getName()} to SQL query for database version.
     */
    private final Map<String, String> databaseNameToHealthQuery;

    public R2dbcHealthConfiguration() {
        this.databaseNameToHealthQuery = new HashMap<>(12);
        this.databaseNameToHealthQuery.put(R2dbcHealthProperties.POSTGRES, R2dbcHealthProperties.COMMON_QUERY);
        this.databaseNameToHealthQuery.put(R2dbcHealthProperties.MARIADB, R2dbcHealthProperties.COMMON_QUERY);
        this.databaseNameToHealthQuery.put(R2dbcHealthProperties.MYSQL, R2dbcHealthProperties.COMMON_QUERY);
        this.databaseNameToHealthQuery.put(R2dbcHealthProperties.MSSQL, R2dbcHealthProperties.MSSQL_QUERY);
    }

    /**
     * @return true if health indicator is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled value to set for autoconfiguration
     */
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void setDatabaseNameToHealthQuery(@MapFormat(transformation = MapFormat.MapTransformation.FLAT, keyFormat = StringConvention.RAW) Map<String, String> databaseNameToHealthQuery) {
        if (CollectionUtils.isNotEmpty(databaseNameToHealthQuery)) {
            this.databaseNameToHealthQuery.putAll(databaseNameToHealthQuery);
        }
    }

    /**
     * @param metadataName name of r2dbc driver from metadata {@link io.r2dbc.spi.ConnectionFactoryMetadata#getName()}
     * @return SQL query to return version for specified database
     * @see R2dbcHealthProperties#COMMON_QUERY
     */
    public Optional<String> getHealthQuery(String metadataName) {
        return Optional.ofNullable(databaseNameToHealthQuery.get(metadataName));
    }
}
