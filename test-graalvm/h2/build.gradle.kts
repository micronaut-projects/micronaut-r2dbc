plugins {
    id("io.micronaut.build.internal.r2dbc-testproject")
}

dependencies {
    runtimeOnly(libs.managed.r2dbc.h2)
    runtimeOnly(mnSql.h2)
}
