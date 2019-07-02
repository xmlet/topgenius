package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class LastfmExpected {

    static final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
    static final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";
    static final String LASTFM_GEOGRAPHIC_TOP_TRACKS = "?method=geo.gettoptracks&format=json&country=%s&page=%d&api_key=%s";

    static String expectedArtistTopTrack(String mbid) throws IOException {
        final String LASTFM_ARTIST_TOP_TRACKS = LASTFM_HOST
                                            + "?method=artist.gettoptracks&format=json&mbid=%s&page=%d&api_key="
                                            + LASTFM_API_KEY;
        String path = String.format(LASTFM_ARTIST_TOP_TRACKS, mbid, 1);
        InputStream in = new URL(path).openStream();
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")));
        reader.beginObject(); // enter root
        reader.nextName();
        reader.beginObject(); // enter "toptracks"
        reader.nextName();
        reader.beginArray();  // enter "track"
        reader.beginObject(); // enter "track[]"
        reader.nextName();    // enter "track[]::name"
        String res = reader.nextString();
        reader.close();
        return res;
    }

    static List<String> expectedCountryPages(MockAsyncRequest areq, String country, int limit) throws IOException {
        final String path = LASTFM_HOST + LASTFM_GEOGRAPHIC_TOP_TRACKS;
        final int pages = limit / 50;
        return IntStream
            .rangeClosed(1, pages)
            .mapToObj(page -> {
                String url = String.format(
                    LASTFM_HOST + LASTFM_GEOGRAPHIC_TOP_TRACKS,
                    country, page, LASTFM_API_KEY);
                return areq.get(url).join().body();
            })
            .collect(toList());
    }

    static String expectedCountryTopTrack(AsyncRequest areq, String country, int page) throws IOException {
        final String path = LASTFM_HOST + LASTFM_GEOGRAPHIC_TOP_TRACKS;
        String url = String.format(path, country, page, LASTFM_API_KEY);
        String body = areq.get(url).join().body();
        JsonReader reader = new Gson().newJsonReader(new StringReader(body));
        reader.beginObject(); // enter root
        reader.nextName();
        reader.beginObject(); // enter "tracks"
        reader.nextName();
        reader.beginArray();  // enter "track"
        reader.beginObject(); // enter "track[]"
        reader.nextName();    // enter "track[]::name"
        String res = reader.nextString();
        reader.close();
        return res;
    }
    static int expectedCountryPages(AsyncRequest areq, String country) {
        try {
            Method geoTopTracks = LastfmWebApi.class.getDeclaredMethod("geoTopTracks", String.class, int.class);
            geoTopTracks.setAccessible(true);
            var json = (CompletableFuture<String>) geoTopTracks.invoke(new LastfmWebApi(areq), country, 1);
            var dto = json.thenApply(body -> new Gson().fromJson(body, GeographicTopTracks.class));
            return dto.join().getTracks().getAttr().getTotalPages();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
