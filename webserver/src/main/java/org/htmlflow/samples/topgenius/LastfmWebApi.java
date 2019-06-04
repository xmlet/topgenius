package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;
import org.htmlflow.samples.topgenius.model.GetTopTracks;
import org.htmlflow.samples.topgenius.model.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class LastfmWebApi {
    private static final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
    private static final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";

    private static final String LASTFM_ARTIST_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=artist.gettoptracks&format=json&mbid=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;

    private static final String LASTFM_GEOGRAPHIC_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=geo.gettoptracks&format=json&country=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;
    protected final Gson gson;

    public LastfmWebApi() {
        this.gson = new Gson();
    }

    public Track[] geographicTopTracks(String country, int page){
        String path = String.format(LASTFM_GEOGRAPHIC_TOP_TRACKS, country, page);
        GeographicTopTracks dto = gson.fromJson(request(path), GeographicTopTracks.class);
        if(dto.getTracks() == null)
            return new Track[0];
        else
            return dto.getTracks().getTrack();
    }

    /**
     * Request are performed only on-demand per page.
     * Since iterate() returns a lazy sequence then no requests
     * are made until we start iterating through the resulting Iterator.
     */
    public Stream<Track> geographicTopTracks(String country){
        return IntStream
            .iterate(1, n -> n + 1)
            .mapToObj(p -> geographicTopTracks(country, p))
            .takeWhile(arr -> arr.length != 0)
            .flatMap(Stream::of);
    }

    public Track[] artistTopTracks(String artisMbid, int page){
        String path = String.format(LASTFM_ARTIST_TOP_TRACKS, artisMbid, page);
        GetTopTracks dto = gson.fromJson(request(path), GetTopTracks.class);
        return dto.getToptracks().getTrack();
    }

    /**
     * Request are performed only on-demand per page.
     * Since iterate() returns a lazy sequence then no requests
     * are made until we start iterating through the resulting Iterator.
     */
    public Stream<Track> artistTopTracks(String artistMbid) {
        return IntStream
            .iterate(1, n -> n + 1)
            .mapToObj(p -> artistTopTracks(artistMbid, p))
            .takeWhile(arr -> arr.length != 0)
            .flatMap(Stream::of);
    }

    static String request(String path) {
        try {
            System.out.println(path);
            InputStream in = new URL(path).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.lines().collect(joining());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
