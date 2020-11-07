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
package io.micronaut.r2dbc.graalvm;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.graal.AutomaticFeatureUtils;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

/**
 * Automatic feature for R2DBC drivers.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Internal
@AutomaticFeature
final class R2dbcAutomaticFeature implements Feature {
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        Class<?> mariaDriver = access.findClassByName("org.mariadb.r2dbc.MariadbConnectionFactoryProvider");
        // handle mariadb
        if (mariaDriver != null) {

            AutomaticFeatureUtils.initializeAtRunTime(
                    access,
                    "org.mariadb.r2dbc.MariadbConnectionFactoryProvider",
                        "org.mariadb.r2dbc.client.ClientBase",
                        "org.mariadb.r2dbc.message.flow.AuthenticationFlow",
                        "org.mariadb.r2dbc.util.DefaultHostnameVerifier"
                    );

            RuntimeClassInitialization.initializeAtRunTime("org.mariadb.r2dbc.message.server");
        }
    }
}
