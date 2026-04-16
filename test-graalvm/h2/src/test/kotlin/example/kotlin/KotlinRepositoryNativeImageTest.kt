package example.kotlin

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import testgraalvm.domain.Owner
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class KotlinRepositoryNativeImageTest {

    @Inject
    lateinit var operations: R2dbcOperations

    @Inject
    lateinit var repository: KotlinOwnerRepository

    @Test
    fun kotlinRepositorySaveUsesRepositoryOperationsInNativeImage() {
        val saved = Mono.from(operations.withTransaction {
            repository.save(Owner("Wilma", 36))
        }).block()

        assertNotNull(saved)
        assertNotNull(saved!!.id)
        assertEquals("Wilma", saved.name)

        val fetched = repository.findByName("Wilma").block()
        assertNotNull(fetched)
        assertEquals(saved.id, fetched!!.id)
    }
}
