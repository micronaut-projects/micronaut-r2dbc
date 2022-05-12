/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.r2dbc.health;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.r2dbc.config.R2dbcHealthConfiguration;
import io.r2dbc.spi.ConnectionFactory;

/**
 * Supports databases according to {@link R2dbcHealthConfiguration#getHealthQuery(String)}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.1.0
 * @deprecated Not used anymore
 */
@Deprecated
class R2dbcHealthCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context) {
        final ConnectionFactory factory = context.getBean(ConnectionFactory.class);
        final R2dbcHealthConfiguration healthConfiguration = context.getBean(R2dbcHealthConfiguration.class);

        final String metadataName = factory.getMetadata().getName();

        return healthConfiguration.getHealthQuery(metadataName).isPresent();
    }
}
