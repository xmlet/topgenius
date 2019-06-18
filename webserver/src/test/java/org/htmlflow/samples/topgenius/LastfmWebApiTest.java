package org.htmlflow.samples.topgenius;

import org.htmlflow.samples.topgenius.LastfmWebApi.Pair;
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

    @Test
    public void getGeographicTopTracksFirstPageInAustralia() throws IOException {
        LastfmWebApi api = new LastfmWebApi();
        Track[] tracks = api.countryTopTracks("Australia", 3).join();
        String expected = expectedCountryTopTrack("Australia", 3);
        assertEquals(expected, tracks[0].getName());
    }

    @Test
    public void getGeographicTopTracksInAustralia() throws IOException, NoSuchFieldException, IllegalAccessException {
        int ausPages = expectedCountryPages("Australia");
        LastfmWebApi api = new LastfmWebApi();
        int[] count = {0};
        api.onRequest(path -> count[0]++);
        // api.onRequest(path -> System.out.println(path));
        api.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        Track track = api.countryTopTracks("Australia").skip(100).findFirst().get();
        String expected = expectedCountryTopTrack("Australia", 3); // 50 tracks per page
        assertEquals(expected, track.getName());
        System.out.println("REQUESTS: " + count[0]);
        int prev = count[0];
        /*
         * Check internal cache of LastfmWebApi
         * All requests were dispatched and their CFs stored in a internal cache.
         */
        Field cache = LastfmWebApi.class.getDeclaredField("countryCache");
        cache.setAccessible(true);
        var map = (Map<String, Pair<Long, List<CompletableFuture<Track[]>>>>) cache.get(api);
        assertEquals(ausPages, map.get("australia").val.size());
        /*
         * Running again should get requests from internal cache of CFs.
         * Skipping more tracks we have to wait for completion of further requests.
         */
        api
            .countryTopTracks("Australia")
            .skip(800)
            .findFirst()
            .get()
            .getName();
        System.out.println("REQUESTS: " + count[0]);
        assertTrue(count[0] > prev);
    }
}
