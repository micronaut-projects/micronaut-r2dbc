package io.micronaut.data.r2dbc

import io.micronaut.data.repository.reactive.RxJavaCrudRepository
import io.micronaut.data.tck.entities.Product

interface ProductReactiveRepository extends RxJavaCrudRepository<Product, Long> {
}
