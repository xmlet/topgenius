package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

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
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in)));
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

    static String expectedCountryTopTrack(String country, int page) throws IOException {
        final String path = LASTFM_HOST + LASTFM_GEOGRAPHIC_TOP_TRACKS;
        String url = String.format(path, country, page, LASTFM_API_KEY);
        InputStream in = new URL(url).openStream();
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in)));
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
    static int expectedCountryPages(String country) throws IOException {
        final String path = LASTFM_HOST + LASTFM_GEOGRAPHIC_TOP_TRACKS;
        String url = String.format(path, country, 1, LASTFM_API_KEY);
        try {
            Method geoTopTracks = LastfmWebApi.class.getDeclaredMethod("geoTopTracks", String.class);
            geoTopTracks.setAccessible(true);
            var dto = (CompletableFuture<GeographicTopTracks>) geoTopTracks.invoke(new LastfmWebApi(), url);
            return dto.join().getTracks().getAttr().getTotalPages();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
