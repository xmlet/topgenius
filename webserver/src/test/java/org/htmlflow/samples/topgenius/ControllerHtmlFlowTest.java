package org.htmlflow.samples.topgenius;

import org.htmlflow.samples.topgenius.controllers.ControllerHtmlFlow;
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

import static io.vertx.core.Vertx.vertx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ControllerHtmlFlowTest {

    @Test
    public void handlebarsTopTracks() throws IOException, URISyntaxException {
        /**
         * Arrange
         */
        MockAsyncRequest areq = new MockAsyncRequest();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx(), areq);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerHtmlFlow ctrl = new ControllerHtmlFlow(sessions);
        /**
         * Expect
         */
        URL url = ControllerHtmlFlowTest.class.getClassLoader().getResource("topgenius/hf-australia-50.html");
        URI uri = url.toURI();
        Iterator<String> expected = Files
            .lines(Paths.get(uri))
            .filter(l -> !l.contains("0.")) // Ignore line with Server processing time
            .iterator();
        /**
         * Act
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "8");
        ctrl.toptracksHandler(ctx);
        String body = ctx.join();
        String[] lines = body.split("\n");
        Iterator<String> actual = Arrays
            .stream(lines)
            .filter(l -> !l.contains("0."))// Ignore line with Server processing time
            .iterator();
        /**
         * Assert
         */
        assertTrue(expected.hasNext());
        while(expected.hasNext()) {
            var e = expected.next();
            var a = actual.next();
            assertEquals(e, a);
        }
        assertFalse(actual.hasNext());
    }
}
