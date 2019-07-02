package org.htmlflow.samples.topgenius;

import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.htmlflow.samples.topgenius.LastfmExpected.expectedArtistTopTrack;
import static org.htmlflow.samples.topgenius.LastfmExpected.expectedCountryPages;
import static org.htmlflow.samples.topgenius.LastfmExpected.expectedCountryTopTrack;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class LastfmWebApiTest {

    @Test
    public void getTopTracksFromMuse() throws IOException {
        LastfmWebApi api = new LastfmWebApi();
        String mbid = "fd857293-5ab8-40de-b29e-55a69d4e4d0f";
        Track[] tracks = api.artistTopTracks(mbid, 1).join();
        String expected = expectedArtistTopTrack(mbid);
        assertEquals(expected, tracks[0].getName());
    }

    // @Test
    public void getGeographicTopTracksInAustralia() throws IOException, NoSuchFieldException, IllegalAccessException {
        AsyncRequest areq = new MockAsyncRequest();
        int ausPages = expectedCountryPages(areq, "Australia");
        LastfmWebApi api = new LastfmWebApi(areq);
        int[] count = {0};
        api.onRequest(path -> count[0]++);
        // api.onRequest(path -> System.out.println(path));
        api.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        Track track = api.countryTopTracks("Australia", 10000, true).skip(100).findFirst().get();
        String expected = expectedCountryTopTrack(areq, "Australia", 3); // 50 tracks per page
        assertEquals(expected, track.getName());
        System.out.println("REQUESTS: " + count[0]);
        int prev = count[0];
        /*
         * Check internal cache of LastfmWebApi
         * All requests were dispatched and their CFs stored in a internal cache.
         */
        Field cache = LastfmWebApi.class.getDeclaredField("cacheJsonPages");
        cache.setAccessible(true);
        var map = (Map<String, List<CompletableFuture<String>>>) cache.get(api);
        assertEquals(ausPages, map.get("australia").size());
        cache = LastfmWebApi.class.getDeclaredField("cacheTracksPages");
        cache.setAccessible(true);
        var map2 = (Map<String, List<CompletableFuture<Track[]>>>) cache.get(api);
        assertEquals(ausPages, map2.get("australia").size());
        /*
         * Running again should get requests from internal cache of CFs.
         * Skipping more tracks we have to wait for completion of further requests.
         */
        api
            .countryTopTracks("Australia", 10000, true)
            .skip(800)
            .findFirst()
            .get()
            .getName();
        System.out.println("REQUESTS: " + count[0]);
        assertTrue(count[0] > prev);
        api.clearCacheAndCancelRequests("australia");
    }

    @Test
    public void testAndClearCache() throws InterruptedException {
        LastfmWebApi api = new LastfmWebApi(new MockAsyncRequest());
        int[] count = {0};
        api.onRequest(path -> count[0]++);
        api.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        Track track = api.countryTopTracks("Australia", 10000, true).findFirst().get();
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
        api.clearCacheAndCancelRequests("australia");
        prev = count[0];
        System.out.println("REQUESTS: " + prev);
        Thread.currentThread().sleep(200);
        assertEquals(prev, count[0]);
    }
}
