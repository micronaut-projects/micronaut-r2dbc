# Python Docs Disabled Test Inventory

This file tracks the Python documentation examples under `doc-examples/example-python`
that are present but disabled, or that deviate from the Java example because the direct port does
not compile or does not behave like the Java example yet. It is the bug-fixing task list for
the Python compiler (`micronaut-inject-python` / `micronaut-context-python`); every row references a
`TODO(python)` comment in the sources or a workaround described below.

The Python examples are compiled by every build and their tests run with
`./gradlew pythonCheck -Ppython-ci` (the "Python CI" GitHub workflow). The tests start a MySQL
Testcontainers container (`mysql:8.4.5`) like the Java, Kotlin and Groovy example tests.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\(" doc-examples/example-python/src`.
- Last full-suite command: `./gradlew :micronaut-doc-examples:micronaut-example-python:test -Ppython-ci`.
- Last full-suite result: build successful, 4 tests executed, 0 skipped, 0 failures (micronaut-core 5.2.3, micronaut-build 8.1.2).

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets.
  Standard Micronaut and Micronaut Data annotations are imported from their Java package
  (`micronaut.data.annotation`, `micronaut.data.r2dbc.annotation`, `micronaut.http.annotation`, ...).
- The application code of the `source="main"` snippets lives in `src/main/python`, the tests in `src/test/python`
  (two source roots compiled by `compilePython` / `compileTestPython`; the Micronaut processors are `implementation`
  dependencies so they are on the main compile classpath too). Classes of the main root are imported with absolute
  imports (`from example.Author import Author`).
- Entities are `@Serdeable @MappedEntity @dataclass` classes with `id: Annotated[int | None, Id, GeneratedValue] = None`;
  relations use `Annotated[Author | None, Relation("MANY_TO_ONE")]`.
- Repositories are classes extending the generic Java repository interface (`ReactiveStreamsCrudRepository[Author, int]`)
  with `...` bodies; query method names stay camelCase (`findById`, `findAll`) because the name is parsed by Micronaut Data;
  the `findById(self, id: int)` overrides replace the inherited method like the Java `Mono<Author> findById(Integer)`.
- Reactive results reach Python as plain `Publisher` objects: wrap them with `Mono.from_(...)` / `Flux.from_(...)`
  (`from_` is the keyword-safe alias of `Mono.from`).
- Controller methods return `Mono[Author]` / `Flux[Author]` like the Java example; repository methods declared with a
  `Publisher[Book]` return type keep it.
- The MySQL container properties of the Java tests' `TestPropertyProvider` are supplied by the Java
  `@ContextConfigurer` `example.support.MySqlTestConfigurer` (`configure(ApplicationContext)`, gated on the `mysql`
  environment of `@MicronautTest(environments=["mysql"])`) because Micronaut Test calls `TestPropertyProvider` before the
  GraalPy runtime exists.
- Python tests are `@MicronautTest` classes with `@BeforeEach`/`@AfterEach` methods (the Java `@BeforeAll`/`PER_CLASS`
  lifecycle is not available); `AuthorControllerTest` (Python only) exercises the guide's `Author`, `AuthorRepository` and
  `AuthorController` snippets over HTTP, `BookControllerTest` the programmatic transactions of the `BookRepository`.
- Java classes are imported (`from reactor.core.publisher import Flux, Mono`, `from org.reactivestreams import Publisher`);
  no `java.type(...)` alias is needed by these examples.

## Active `@Disabled` Tests

None.

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `example.BookRepository` (`mandatory` tag) | Only `save` is redeclared with `@Transactional("MANDATORY")`; the `saveAll` override of the Java example is omitted (`TODO(python)`): overriding the inherited generic `<S extends Book> Publisher<S> saveAll(Iterable<S>)` is not possible yet, a `list[Book]` hint produces `Publisher<Book> saveAll(Iterable<Book>)` which clashes with the inherited erasure, and a PEP 695 type parameter (`def saveAll[S: Book](...) -> Publisher[S]`) is rejected by the Micronaut Data visitor (`Unsupported return type for a save method: python.S`). |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

None.
