plugins {
    id("io.micronaut.build.internal.r2dbc-testproject")
    id("io.micronaut.build.internal.kotlin-kapt")
    id("io.micronaut.build.internal.r2dbc-kotlin")
}

tasks.named("internalStartTestResourcesService") {
    enabled = false
}

tasks.named("startTestResourcesService") {
    enabled = false
}

tasks.named("stopTestResourcesService") {
    enabled = false
}

dependencies {
    runtimeOnly(libs.managed.r2dbc.h2)
    runtimeOnly(mnSql.h2)
    kaptTest(mnData.micronaut.data.processor)
    testImplementation(mnKotlin.micronaut.kotlin.runtime)
}
