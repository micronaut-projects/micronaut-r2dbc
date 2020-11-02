package example

import groovy.transform.Immutable
import io.micronaut.core.annotation.Introspected

@Introspected
@Immutable
class Book {
    String title
    int pages
}
