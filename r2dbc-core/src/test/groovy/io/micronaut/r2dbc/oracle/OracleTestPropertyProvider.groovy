package io.micronaut.r2dbc.oracle


import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.OracleContainer
import org.testcontainers.utility.DockerImageName

trait OracleTestPropertyProvider implements TestPropertyProvider {

    @Override
    Map<String, String> getProperties() {
        def dbContainer = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
                .withEnv("ORACLE_PASSWORD", "password")
                .withPassword("password")
        dbContainer.start()
        Map<String, String> props = [
                "r2dbc.datasources.default.options.applicationName": "test",
                "r2dbc.datasources.default.host"     : dbContainer.getHost(),
                "r2dbc.datasources.default.port"     : dbContainer.getFirstMappedPort(),
                "r2dbc.datasources.default.driver"   : "oracle",
                "r2dbc.datasources.default.username" : dbContainer.getUsername(),
                "r2dbc.datasources.default.password" : dbContainer.getPassword(),
                "r2dbc.datasources.default.schema-generate" : "CREATE_DROP",
                "r2dbc.datasources.default.dialect" : "ORACLE"
        ]
        try {
            def database = dbContainer.getDatabaseName()
            props.put("r2dbc.datasources.default.database" , database)
        } catch (e) {
        }
        return props
    }
}
