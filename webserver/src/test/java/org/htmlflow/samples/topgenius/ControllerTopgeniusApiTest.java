package org.htmlflow.samples.topgenius;

import io.vertx.core.Vertx;
import org.htmlflow.samples.topgenius.controllers.ControllerTopgeniusApi;
import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ControllerTopgeniusApiTest {

    @Test
    public void testTogeniusApi() throws IOException {
        LastfmWebApi api = new LastfmWebApi();
        int[] count = {0};
        api.onRequest(path -> count[0]++);
        api.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(api, Vertx.vertx());
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "150");
        ctrl.topTracksHandler(ctx);
        String[] pages = ctx.complete.join().split("\n");
        List<String> expected = LastfmExpected.expectedCountryPages("australia", 150);
        assertArrayEquals(expected.toArray(), pages);
    }

    @Test
    public void testAndClearCache() throws InterruptedException {
        LastfmWebApi api = new LastfmWebApi();
        int[] count = {0};
        api.onRequest(path -> count[0]++);
        api.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerTopgeniusApi ctrl = new ControllerTopgeniusApi(api, Vertx.vertx());
        /**
         * Make a request and wait
         */
        MockRountingContext ctx = new MockRountingContext()
            .add("country", "australia")
            .add("limit", "50");
        ctrl.topTracksHandler(ctx);
        ctx.complete.join();
        int prev = count[0];
        System.out.println("REQUESTS: " + prev);
        /*
         * While sleeping more requests have completed and the count should increase.
         */
        Thread.currentThread().sleep(2000);
        System.out.println("REQUESTS: " + count[0]);
        assertTrue(count[0] > prev);
        /*
         * Clearing the cache and cancelling all further requests the count should
         * remain on the same value.
         */
        ctx = new MockRountingContext()
            .add("country", "australia");
        ctrl.clearcacheHandler(ctx);
        prev = count[0];
        System.out.println("REQUESTS: " + prev);
        Thread.currentThread().sleep(2000);
        assertEquals(prev, count[0]);
    }

}
