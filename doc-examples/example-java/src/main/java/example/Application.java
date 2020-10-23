package example;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Singleton;

@Singleton
public class Application {
    private final RxConnectionFactory connectionFactory;

    public Application(RxConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        connectionFactory.withTransaction((connection) -> connection.createBatch()
            .add("CREATE TABLE BOOKS(TITLE VARCHAR(255), PAGES INT)")
            .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Stand', 1000)")
            .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Shining', 400)")
            .execute()
        ).blockingSubscribe();
    }

    public static void main(String...args) {
        Micronaut.run(args);
    }
}
