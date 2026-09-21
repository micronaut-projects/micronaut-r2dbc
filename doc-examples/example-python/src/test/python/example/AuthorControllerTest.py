from typing import Annotated

from jakarta.inject import Inject
from micronaut.http.annotation import Get
from micronaut.http.client.annotation import Client
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import AfterEach, BeforeEach, Test
from reactor.core.publisher import Flux, Mono

from example.Author import Author
from example.AuthorRepository import AuthorRepository


@Client("/authors")
class AuthorClient:
    @Get("/")
    def list(self) -> list[Author]: ...


# The MySQL container properties are supplied by example.support.MySqlTestConfigurer for the "mysql" environment
@MicronautTest(transactional=False, environments=["mysql"])
class AuthorControllerTest:

    authorClient: Annotated[AuthorClient, Inject]
    authorRepository: Annotated[AuthorRepository, Inject]

    @BeforeEach
    def setup_data(self):
        Flux.from_(self.authorRepository.saveAll([
            Author("Stephen King"),
            Author("James Patterson"),
        ])).collectList().block()

    @AfterEach
    def cleanup(self):
        Flux.from_(self.authorRepository.deleteAll()).collectList().block()

    @Test
    def test_list_authors(self):
        authors = self.authorClient.list()
        assert len(authors) == 2
        assert {author.name for author in authors} == {"Stephen King", "James Patterson"}

    @Test
    def test_find_author_by_id(self):
        stephen_king = Flux.from_(self.authorRepository.findAll()).filter(lambda author: author.name == "Stephen King").blockFirst()
        assert stephen_king is not None
        author = Mono.from_(self.authorRepository.findById(stephen_king.id)).block()
        assert author is not None
        assert author.name == "Stephen King"
