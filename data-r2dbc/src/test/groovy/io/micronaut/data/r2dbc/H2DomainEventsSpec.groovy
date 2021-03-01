package io.micronaut.data.r2dbc

import io.micronaut.data.tck.repositories.DomainEventsReactiveRepository
import io.micronaut.data.tck.repositories.DomainEventsRepository
import io.micronaut.data.tck.tests.AbstractEventsSpec

class H2DomainEventsSpec extends AbstractEventsSpec implements H2TestPropertyProvider {
    @Override
    DomainEventsRepository eventsRepository() {
        return context.getBean(H2DomainEventsRepository)
    }

    @Override
    DomainEventsReactiveRepository eventsReactiveRepository() {
        return context.getBean(H2ReactiveDomainEventsRepository)
    }
}
