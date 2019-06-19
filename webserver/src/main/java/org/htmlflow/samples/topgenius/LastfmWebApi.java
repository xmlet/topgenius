package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;
import org.htmlflow.samples.topgenius.model.GetTopTracks;
import org.htmlflow.samples.topgenius.model.Track;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.CompletableFuture.completedFuture;


public class LastfmWebApi {
    private static final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
    private static final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";

    private static final String LASTFM_ARTIST_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=artist.gettoptracks&format=json&mbid=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;

    private static final String LASTFM_GEOGRAPHIC_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=geo.gettoptracks&format=json&country=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;
    private static final long TTL = 1000*60*60*24;

    private final Map<String, TtlResponse> countryCache;
    private final Gson gson;
    private AsyncRequest req;


    public LastfmWebApi() {
        this(new WebRequest());
    }

    public LastfmWebApi(AsyncRequest req) {
        this.gson = new Gson();
        this.req = req;
        this.countryCache = new HashMap<>();
    }

    private static String geoTopTracksPath(String country, int page) {
        return String.format(LASTFM_GEOGRAPHIC_TOP_TRACKS, country, page);
    }

    private CompletableFuture<String> geoTopTracks(String country, int page) {
        return req
            .get(geoTopTracksPath(country, page))
            .thenApply(HttpResponse::body);
    }

    private CompletableFuture<Track[]> tracksFromJson(CompletableFuture<String> curr) {
        return curr
            .thenApply(body -> gson.fromJson(body, GeographicTopTracks.class))
            .thenApply(dto -> dto.getTracks() == null
                    ? new Track[0]
                    : dto.getTracks().getTrack()
            );
    }

    public CompletableFuture<Track[]> countryTopTracks(String country, int page){
        return tracksFromJson(geoTopTracks(country, page));
    }

    /**
     * All requests are performed sequentially.
     * Yet, requests are asynchronous and we do not wait for response completion.
     */
    public Stream<String> countryJson(String country){
        return getOrCreateTopTracks(country)
            .json
            .stream()
            .map(CompletableFuture::join);
    }

    /**
     * All requests are performed sequentially.
     * Yet, requests are asynchronous and we do not wait for response completion.
     */
    public Stream<Track> countryTopTracks(String country){
        return getOrCreateTopTracks(country)
            .tracks
            .stream()
            .map(CompletableFuture::join)
            .takeWhile(arr -> arr.length != 0)
            .flatMap(Stream::of);

    }


    private TtlResponse getOrCreateTopTracks(String country) {
        country = country != null ? country.toLowerCase() : "";
        var pair = countryCache.computeIfAbsent(country, this::createCountryTopTracks);
        long dur = currentTimeMillis() - pair.date;
        if(dur > TTL) {
            pair = createCountryTopTracks(country);
            countryCache.put(country, pair);
        }
        return pair;
    }

    /**
     * Since streams are lazy, then we collect it to force requests to be dispatched.
     * Requests to geoTopTracks() are made sequentially.
     */
    private TtlResponse createCountryTopTracks(String country) {
        // Waisting a first page request just to get the total number of pages
        int totalPages = geoTopTracks(country, 1)
            .thenApply(body -> gson.fromJson(body, GeographicTopTracks.class))
            .join()
            .getTracks()
            .getAttr()
            .getTotalPages();
        /**
         * Every request through geoTopTracks() is performed on the
         * completion of the previous request to avoid concurrency and
         * do not exceed rate limits of Last.fm.
         */
        ArrayList<CompletableFuture<String>> seed = new ArrayList<>();
        seed.add(completedFuture(null));
        ArrayList<CompletableFuture<Track[]>> tracks = new ArrayList<>();
        ArrayList<CompletableFuture<String>> json = IntStream
            .rangeClosed(1, totalPages)
            .mapToObj(page -> page) // boxing
            .reduce( // !!! DO NOT replace it by collect() !!!!!
                seed,
                (lst, page) -> { // Enforces sequential requests to not exceed rate limits
                    CompletableFuture<String> curr = lst.get(lst.size() - 1);
                    curr = curr.thenCompose(__ -> geoTopTracks(country, page)); // Next request only performed on completion of the previous one
                    lst.add(curr);
                    tracks.add(tracksFromJson(curr));
                    return lst;
                },
                (l1, l2) -> null); // Keep it as null supplier. No combinator since it is not processed in parallel
        json.remove(0); // Remove the seed that is an empty CF
        return TtlResponse.of(currentTimeMillis(), json, tracks);
    }

    public CompletableFuture<Track[]> artistTopTracks(String artisMbid, int page){
        String path = String.format(LASTFM_ARTIST_TOP_TRACKS, artisMbid, page);
        return req
            .get(path)
            .thenApply(HttpResponse::body)
            .thenApply(body -> {
                GetTopTracks dto = gson.fromJson(body, GetTopTracks.class);
                return dto.getToptracks().getTrack();
            });
    }

    /**
     * Registers a Consumer handler that is invoked whenever it performs
     * an Http request.
     * The consumer is invoked before the request.
     */
    public void onRequest(Consumer<String> cons) {
        final AsyncRequest old = req;
        this.req = path -> {
            cons.accept(path);
            return old.get(path);
        };
    }

    /**
     * Registers a Consumer handler that is invoked whenever an HTTP
     * requested is completed.
     * The consumer is invoked on response completion.
     */
    public void onResponse(Consumer<HttpResponse<String>> cons) {
        final AsyncRequest old = req;
        this.req = path -> old
            .get(path)
            .whenComplete((body, err) -> cons.accept(body));
    }

    /**
     * Removes entry from countryCache for a given country.
     * All incomplete CFs will be canceled.
     * Since each CF is created as a downstream from the previous one, then we just need to
     * cancel the first CF in progress and all dependent CFs will be canceled too.
     * Each CF will propagate cancellation to its direct downstream and so on.
     *
     * @param country
     */
    public void clearCacheAndCancelRequests(String country) {
        country = country.toLowerCase();
        TtlResponse pair = countryCache.get(country);
        if(pair == null) return;
        pair.json
            .stream()
            .filter(cf -> !cf.isDone())
            .findFirst()
            .ifPresent(cf -> cf.cancel(true));
        countryCache.remove(country);
    }

    static class TtlResponse {
        final long date;
        final List<CompletableFuture<String>> json;
        final List<CompletableFuture<Track[]>> tracks;

        public TtlResponse(long date, List<CompletableFuture<String>> json, List<CompletableFuture<Track[]>> tracks) {
            this.date = date;
            this.json = json;
            this.tracks = tracks;
        }

        static TtlResponse of(long date, List<CompletableFuture<String>> json, List<CompletableFuture<Track[]>> tracks) {
            return new TtlResponse(date, json, tracks);
        }
    }
}
