package org.htmlflow.samples.topgenius;

import io.vertx.core.Vertx;
import org.htmlflow.samples.topgenius.controllers.ControllerSessionsForLastfm;
import org.htmlflow.samples.topgenius.controllers.ControllerTopgeniusApi;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ControllerTopgeniusApiTest {

    @Test
    public void testTogeniusApi() throws IOException {
        Vertx vertx = Vertx.vertx();
        MockAsyncRequest areq = new MockAsyncRequest();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx, areq);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(sessions, vertx);
        /**
         * Perform requests
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "150")
            .addHeader("referer", "dummy");
        sessions.initHandler(ctx);
        ctrl.topTracksHandler(ctx);
        String body = ctx.join();
        String[] pages = body.split("\n");
        List<String> expected = LastfmExpected.expectedCountryPages(areq, "australia", 150);
        assertArrayEquals(expected.toArray(), pages);
        /*
         * Request again now from cache.
         */
        ctrl.topTracksHandler(ctx); // getting response from cts will instantiate a new MockResponse object.
        body = ctx.join();
        pages = body.split("\n");
        assertArrayEquals(expected.toArray(), pages);
        sessions.clearcacheHandler(ctx);
    }

    @Test
    public void testTogeniusApiWithoutCache() throws IOException {
        Vertx vertx = Vertx.vertx();
        MockAsyncRequest areq = new MockAsyncRequest();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx, areq);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(sessions, vertx);
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "150");
        ctrl.topTracksHandler(ctx);
        String[] pages = ctx.join().split("\n");
        List<String> expected = LastfmExpected.expectedCountryPages(areq, "australia", 150);
        assertArrayEquals(expected.toArray(), pages);
    }

    @Test
    public void testWithoutCache() throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx, new MockAsyncRequest());
        int[] count = {0};
        sessions.onRequest(path -> count[0]++);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(sessions, Vertx.vertx());
        /**
         * Make a request and join for completion
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "150");
        ctrl.topTracksHandler(ctx);
        ctx.join();
        int prev = count[0];
        System.out.println("REQUESTS: " + prev);
        /*
         * Even sleeping there are NO more requests because we did not request to cache.
         */
        Thread.currentThread().sleep(200);
        System.out.println("REQUESTS: " + count[0]);
        assertEquals(prev, count[0]);
    }


    @Test
    public void testAndClearCache() throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        ControllerSessionsForLastfm sessions = new ControllerSessionsForLastfm(vertx, new MockAsyncRequest());
        int[] count = {0};
        sessions.onRequest(path -> count[0]++);
        sessions.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(sessions, Vertx.vertx());
        /**
         * Make a request and join for completion
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "50")
            .addHeader("referer", "dummy");
        sessions.initHandler(ctx); // Initialize cache and set session cookie
        ctrl.topTracksHandler(ctx);
        ctx.join();
        int prev = count[0];
        System.out.println("REQUESTS: " + prev);
        /*
         * While sleeping more requests have completed and the count should increase.
         */
        Thread.currentThread().sleep(200);
        System.out.println("REQUESTS: " + count[0]);
        assertTrue(count[0] > prev);
        /*
         * Clearing the cache and cancelling all further requests the count should
         * remain on the same value.
         */
        ctrl.clearcacheHandler(ctx);
        prev = count[0];
        Thread.currentThread().sleep(200);
        System.out.println("REQUESTS: " + prev);
        assertEquals(prev, count[0]);
    }

}
