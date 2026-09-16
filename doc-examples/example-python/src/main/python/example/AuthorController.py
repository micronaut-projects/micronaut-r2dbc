from micronaut.core.async_.annotation import SingleResult
from micronaut.http.annotation import Controller, Get
from org.reactivestreams import Publisher

from example.Author import Author
from example.AuthorRepository import AuthorRepository


@Controller("/authors")
class AuthorController:

    def __init__(self, repository: AuthorRepository):
        self.repository = repository

    @Get
    def all(self) -> Publisher[Author]:  # <1>
        return self.repository.findAll()

    @Get("/id")
    @SingleResult
    def get(self, id: int) -> Publisher[Author]:  # <2>
        return self.repository.findById(id)
