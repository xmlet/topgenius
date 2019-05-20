package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class JingleWebServiceTest {


    static int getExpectedResultsForSeachHiper() throws IOException {
        String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
        String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";
        String LASTFM_SEARCH = LASTFM_HOST
                                          + "?method=artist.search&format=json&artist=%s&api_key="
                                          + LASTFM_API_KEY;
        String path = String.format(LASTFM_SEARCH, "hiper");
        InputStream in = new URL(path).openStream();
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in)));
        reader.beginObject(); // enter root
        reader.nextName();
        reader.beginObject(); // enter "results"
        reader.nextName();
        reader.skipValue();   // skip ""opensearch:Query""
        reader.nextName();    // enter "opensearch:totalResults"
        int res = reader.nextInt();
        reader.close();
        return res;
    }

}
