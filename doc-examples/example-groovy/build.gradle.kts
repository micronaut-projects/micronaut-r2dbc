plugins {
    id("groovy")
    id("io.micronaut.build.internal.r2dbc-example")
}

micronaut {
    testRuntime("spock")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    compileOnly(mnSerde.micronaut.serde.processor)

    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(mnSerde.micronaut.serde.processor)
}
