from jakarta.transaction import Transactional
from micronaut.data.annotation import Join
from micronaut.data.model.query.builder.sql import Dialect
from micronaut.data.r2dbc.annotation import R2dbcRepository
from micronaut.data.repository.reactive import ReactiveStreamsCrudRepository
from org.reactivestreams import Publisher
from reactor.core.publisher import Flux, Mono

from example.Book import Book


@R2dbcRepository(dialect=Dialect.MYSQL)  # <1>
class BookRepository(ReactiveStreamsCrudRepository[Book, int]):

    @Join("author")
    def findById(self, id: int) -> Mono[Book]: ...  # <2>

    @Join("author")
    def findAll(self) -> Flux[Book]: ...

    # tag::mandatory[]
    @Transactional("MANDATORY")
    def save(self, entity: Book) -> Publisher[Book]: ...
    # end::mandatory[]
    # The `saveAll` override of the Java example is omitted: Micronaut Data rejects a redeclared save method whose
    # return type is a method type parameter (`Unsupported return type for a save method: python.S`), so neither
    # `def saveAll[S: Book](self, entities: Iterable[S]) -> Publisher[S]` nor the `list[S]` variant is accepted, and a
    # `list[Book]` hint has the same erasure as the inherited `saveAll(Iterable<S>)` without overriding it.
