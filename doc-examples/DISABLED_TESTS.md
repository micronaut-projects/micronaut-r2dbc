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

- Last generated active `@Disabled` count: 2 (one class, one method).
- Last generated command: `rg -n "@Disabled\(" doc-examples/example-python/src`.
- Last full-suite command: `./gradlew :micronaut-doc-examples:micronaut-example-python:test -Ppython-ci`.
- Last full-suite result: build successful, 4 tests executed, 3 skipped, 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets.
  Standard Micronaut and Micronaut Data annotations are imported from their Java package
  (`micronaut.data.annotation`, `micronaut.data.r2dbc.annotation`, `micronaut.http.annotation`, ...).
- The application code of the `source="main"` snippets lives in `src/main/python`, the tests in `src/test/python`;
  both roots are merged into one directory compiled by `compileTestPython` (see the `TODO(python)` in
  `buildSrc/src/main/groovy/io.micronaut.build.internal.r2dbc-python-example.gradle`): compiling them separately
  yields two GraalPy VFS roots whose generated shim modules shadow each other, and the imports of a source file are
  only resolved within its own root. Classes of the other root are imported with absolute imports
  (`from example.Author import Author`).
- Entities are `@Serdeable @MappedEntity @dataclass` classes with `id: Annotated[int | None, Id, GeneratedValue] = None`;
  relations use `Annotated[Author | None, Relation("MANY_TO_ONE")]`.
- Repositories are classes extending the generic Java repository interface (`ReactiveStreamsCrudRepository[Author, int]`)
  with `...` bodies; query method names stay camelCase (`findById`, `findAll`) because the name is parsed by Micronaut Data;
  `id` parameters are `int | None` so no `int` overload is created next to the inherited `Integer` one.
- Reactive results reach Python as plain `Publisher` objects: wrap them with `Mono.from_(...)` / `Flux.from_(...)`
  (`from_` is the keyword-safe alias of `Mono.from`).
- Controller methods returning reactive types declare the Reactive Streams `Publisher` type (`-> Publisher[Author]`),
  single results are annotated with `@SingleResult`: the Java signature of a bridged method typed `Mono`/`Flux` casts the
  converted `Publisher` and fails with `ClassCastException` (a `[.lang-python]` note in `quickStart.adoc` explains the
  `Publisher` return type).
- The MySQL container properties of the Java tests' `TestPropertyProvider` are supplied by the Java
  `@ContextConfigurer` `example.support.MySqlTestConfigurer` (`configure(ApplicationContext)`, gated on the `mysql`
  environment of `@MicronautTest(environments=["mysql"])`) because Micronaut Test calls `TestPropertyProvider` before the
  GraalPy runtime exists.
- Python tests are `@MicronautTest` classes with `@BeforeEach`/`@AfterEach` methods (the Java `@BeforeAll`/`PER_CLASS`
  lifecycle is not available); `AuthorControllerTest` (Python only) exercises the guide's `Author`, `AuthorRepository` and
  `AuthorController` snippets over HTTP while `BookControllerTest` is disabled.
- Java classes are imported (`from reactor.core.publisher import Flux, Mono`, `from org.reactivestreams import Publisher`);
  no `java.type(...)` alias is needed by these examples.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `example.BookControllerTest` (class) | The Reactor transaction context is not propagated into the publishers returned by Python lambdas inside `operations.withTransaction(...)`: the `@Transactional(MANDATORY)` `BookRepository.save`/`saveAll` fail with `NoTransactionException: Expected an existing transaction, but none was found in the Reactive context` (`programmatic-tx` / `programmatic-tx-status` setup of the Java test). |
| `example.AuthorControllerTest.test_find_author_by_id` | The `findById(self, id: int \| None) -> Mono[Author]` override of `AuthorRepository` (callout `<2>` of the guide) is dropped from the repository bean definition: the generated stub keeps a `findById(Integer)` method that is not intercepted by Micronaut Data, so calling it (from Python or from the Java proxy) runs the `...` body and returns `None`. The inherited `findById` of a repository that does not redeclare it, the parameterless `findAll` override and query methods such as `findByName` work. `AuthorController.get` is affected in the same way. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `example.AuthorController` | `-> Publisher[Author]` return types and `@SingleResult` instead of the `Flux<Author>` / `Mono<Author>` of the Java example (see the migration rules). |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

None.
