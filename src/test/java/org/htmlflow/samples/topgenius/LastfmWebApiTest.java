package org.htmlflow.samples.topgenius;

import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Assert;
import org.junit.Test;


public class LastfmWebApiTest {

    @Test
    public void getTopTracksFromMuse() {
        LastfmWebApi api = new LastfmWebApi();
        String mbid = "fd857293-5ab8-40de-b29e-55a69d4e4d0f";
        Track[] tracks = api.getTopTracks(mbid, 1);
        Assert.assertEquals("Supermassive Black Hole", tracks[0].getName());
    }

}
