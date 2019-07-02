package org.htmlflow.samples.topgenius;

import io.vertx.core.Vertx;
import org.htmlflow.samples.topgenius.controllers.ControllerHandlebars;
import org.htmlflow.samples.topgenius.controllers.ControllerSessionsForLastfm;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ControllerHandlebarsTest {

    @Test
    public void handlebarsTopTracks() throws IOException, URISyntaxException {
        /**
         * Arrange
         */
        Vertx vertx = Vertx.vertx();
        MockAsyncRequest areq = new MockAsyncRequest();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx, areq);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerHandlebars ctrl = new ControllerHandlebars(sessions, vertx);
        /**
         * Expect
         */
        URL url = ControllerHandlebarsTest.class.getClassLoader().getResource("topgenius/hbs-australia-50.html");
        URI uri = url.toURI();
        Iterator<String> expected = Files
            .lines(Paths.get(uri))
            .filter(l -> !l.contains("Server processing time")) // Ignore line with Server processing time
            .iterator();
        /**
         * Act
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "50");
        ctrl.toptracksHandler(ctx);
        String[] lines = ctx.join().split("\r\n");
        Iterator<String> actual = Arrays
            .stream(lines)
            .filter(l -> !l.contains("Server processing time"))// Ignore line with Server processing time
            .iterator();
        /**
         * Assert
         */
        assertTrue(expected.hasNext());
        while(expected.hasNext())
            assertEquals(expected.next(), actual.next());
        assertFalse(actual.hasNext());
    }
}
