package example;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.reactivex.Flowable;

import javax.inject.Singleton;

@Singleton
public class Application {
    private final ConnectionFactory connectionFactory;

    public Application(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        Flowable<? extends Connection> flowable = Flowable.fromPublisher(connectionFactory.create());
        flowable.flatMap(connection -> connection.createBatch()
            .add("CREATE TABLE BOOKS(TITLE VARCHAR(255), PAGES INT)")
            .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Stand', 1000)")
            .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Shining', 400)")
        .execute()).blockingSubscribe();
    }

    public static void main(String...args) {
        Micronaut.run(args);
    }
}
