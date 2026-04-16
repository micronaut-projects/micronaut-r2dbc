plugins {
    id("io.micronaut.build.internal.r2dbc-testproject")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.micronaut.build.internal.r2dbc-kotlin")
}

allOpen {
    annotation("io.micronaut.aop.Around")
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
