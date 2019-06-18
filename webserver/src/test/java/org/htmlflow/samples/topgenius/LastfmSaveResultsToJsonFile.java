package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class LastfmSaveResultsToJsonFile {
    private static final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
    private static final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";
    private static final String LASTFM_GEOGRAPHIC_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=geo.gettoptracks&format=json&country=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;

    // @Test
    public void saveAustraliaTopTracks() throws IOException {
        WebRequest req = new WebRequest();
        Gson gson = new Gson();
        String country = "australia";
        for (int page = 1; true; page++) {
            String path = format(LASTFM_GEOGRAPHIC_TOP_TRACKS, country, page);
            InputStream strm = req.get(path).join().body();
            String body = new BufferedReader(new InputStreamReader(strm)).lines().collect(joining());
            GeographicTopTracks dto = gson.fromJson(body, GeographicTopTracks.class);
            if(dto.getTracks() == null || dto.getTracks().getTrack().length == 0)
                break;
            String output = format("lastfm-%s-page-%d.json", country, page);
            Files.writeString(Paths.get(output), body);
            Files.writeString(Paths.get(output), body);
        }
    }
}
