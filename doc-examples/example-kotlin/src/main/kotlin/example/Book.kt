package example

import io.micronaut.core.annotation.Introspected

@Introspected
data class Book(val title : String, val age : Integer)