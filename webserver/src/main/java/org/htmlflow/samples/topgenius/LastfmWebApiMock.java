package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;
import org.htmlflow.samples.topgenius.model.Track;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;


public class LastfmWebApiMock extends LastfmWebApi{
    private static final int BUFFER_SIZE = 1024 * 128;
    private static final String EMPTY_TRACKS = "{ \"tracks\": { \"track\": [] }}";
    private static final Track[] EMPTY_ARRAY_OF_TRACKS = {};
    private static final Gson gson = new Gson();

    public static final int MAX_PAGES = 200;

    static final String[] jsonAustraliaTopTracks = IntStream
        .rangeClosed(1, MAX_PAGES)
        .mapToObj(page -> readResource("australia", page))
        .toArray(size -> new String[size]);

    static final List<Track[]> australiaTopTracksPages =  Stream
        .of(jsonAustraliaTopTracks)
        .map(body -> gson.fromJson(body, GeographicTopTracks.class))
        .map(geoTopTracks -> geoTopTracks.getTracks().getTrack())
        .collect(toList());

    static final List<Track> australiaTopTracks = australiaTopTracksPages
            .stream()
            .flatMap(Stream::of)
            .collect(toList());

    public static String jsonGeographicTopTracks(String country, int page) {
        if(!country.equals("australia") || page > LastfmWebApiMock.MAX_PAGES)
            return EMPTY_TRACKS;
        return jsonAustraliaTopTracks[page - 1];
    }

    @Override
    public Track[] geographicTopTracks(String country, int page){
        if(!country.toLowerCase().equals("australia") || page > australiaTopTracksPages.size())
            return EMPTY_ARRAY_OF_TRACKS;
        return australiaTopTracksPages.get(page -1);
    }

    @Override
    public Stream<Track> geographicTopTracks(String country){
        if(!country.toLowerCase().equals("australia"))
            return Stream.empty();
        return australiaTopTracks.stream();
    }

    @Override
    public Track[] artistTopTracks(String artisMbid, int page){
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Track> artistTopTracks(String artistMbid) {
        throw new UnsupportedOperationException();
    }

    public static String readResource(String country, int page) {
        String path = format("lastfm-toptracks/lastfm-%s-page-%d.json", country, page);
        System.out.println(path);
        URL url = LastfmWebApiMock.class.getClassLoader().getResource(path);
        if(url == null) return null;
        try(InputStream in = url.openStream()) {
            return streamToString(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String streamToString(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
}
