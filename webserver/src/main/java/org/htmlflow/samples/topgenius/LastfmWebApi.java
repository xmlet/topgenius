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
    private static final int TRACKS_PER_PAGE = 50;

    private final Map<String, List<CompletableFuture<String>>> cacheJsonPages;
    private final Map<String, List<CompletableFuture<Track[]>>> cacheTracksPages;
    private final Gson gson;
    private AsyncRequest req;


    public LastfmWebApi() {
        this(new WebRequest());
    }

    public LastfmWebApi(AsyncRequest req) {
        this.gson = new Gson();
        this.req = req;
        this.cacheJsonPages = new HashMap<>();
        this.cacheTracksPages = new HashMap<>();
    }

    /**
     * Top tracks of an artist given its MBID.
     */
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
     * Returns a stream of pages in JSON format.
     */
    public Stream<String> countryTopTracksInJsonPages(String country, int nrOfTracks, boolean cache){
        int nrOfPages = ( nrOfTracks + TRACKS_PER_PAGE - 1)  / TRACKS_PER_PAGE;
        return cache
            ? countryTopTracksInJsonPagesFromCache(country, nrOfPages)
            : countryTopTracksInJsonPages(country, nrOfPages);
    }

    /**
     * Dispatches a number of requests needed to fetch a stream of json pages with the size of nrOfPages.
     */
    public Stream<String> countryTopTracksInJsonPages(String country, int nrOfPages){
        int [] page = {1};
        CompletableFuture<String> seed = CompletableFuture.completedFuture(null);
        return Stream
            .iterate(seed, prev -> prev.thenCompose(__ -> geoTopTracks(country, page[0]++)))
            .skip(1)
            .map(CompletableFuture::join)
            .limit(nrOfPages);
    }

    /**
     * Dispatches all requests for all pages.
     */
    public Stream<String> countryTopTracksInJsonPagesFromCache(String country, int nrOfPages){
        return getOrCreateJsonPages(country)
            .stream()
            .map(CompletableFuture::join)
            .limit(nrOfPages);
    }

    /**
     * All requests are performed sequentially.
     * Yet, requests are asynchronous and we do not wait for response completion.
     */
    public Stream<Track> countryTopTracks(String country, int limit, boolean cache){
        return cache ? countryTopTracksFromCache(country, limit) : countryTopTracks(country, limit);
    }

    /**
     * Dispatches a number of requests needed to fetch a stream of tracks with the size of limit.
     */
    public Stream<Track> countryTopTracks(String country, int limit) {
        int [] page = {1};
        CompletableFuture<String> seed = CompletableFuture.completedFuture(null);
        return Stream
            .iterate(seed, prev -> prev.thenCompose(__ -> geoTopTracks(country, page[0]++)))
            .skip(1)
            .map(cf -> cf.thenApply(body -> gson
                .fromJson(body, GeographicTopTracks.class)
                .getTracks()
                .getTrack()))
            .map(CompletableFuture::join)
            .flatMap(Stream::of)
            .limit(limit);
    }

    /**
     * Dispatches all requests for all pages.
     */
    private Stream<Track> countryTopTracksFromCache(String country, int limit){
        return getOrCreateTracksPages(country)
            .stream()
            .map(CompletableFuture::join)
            .takeWhile(arr -> arr.length != 0)
            .flatMap(Stream::of)
            .limit(limit);
    }

    private static String geoTopTracksPath(String country, int page) {
        return String.format(LASTFM_GEOGRAPHIC_TOP_TRACKS, country, page);
    }

    private CompletableFuture<String> geoTopTracks(String country, int page) {
        return req
            .get(geoTopTracksPath(country, page))
            .thenApply(HttpResponse::body);
    }

    private List<CompletableFuture<String>> getOrCreateJsonPages(String country) {
        final String ctr = country != null ? country.toLowerCase() : "";
        return cacheJsonPages.computeIfAbsent(
            ctr,
            key -> {
                JsonAndTracks pair = createCountryTopTracks(key);
                cacheTracksPages.put(ctr, pair.tracks);
                return pair.json;
            });
    }

    private List<CompletableFuture<Track[]>> getOrCreateTracksPages(String country) {
        final String ctr = country != null ? country.toLowerCase() : "";
        return cacheTracksPages.computeIfAbsent(
            ctr,
            key -> {
                JsonAndTracks pair = createCountryTopTracks(key);
                cacheJsonPages.put(ctr, pair.json);
                return pair.tracks;
            });
    }

    /**
     * Since streams are lazy, then we collect it to force requests to be dispatched.
     * Requests to geoTopTracks() are made sequentially.
     */
    private JsonAndTracks createCountryTopTracks(String country) {
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
                (l1, l2) -> null); // Keep it as null supplier. No combinator needed since it is not processed in parallel
        json.remove(0); // Remove the seed that is an empty CF
        return JsonAndTracks.of(json, tracks);
    }

    /**
     * Chains a continuation to parse Json and get the tracks array.
     */
    private CompletableFuture<Track[]> tracksFromJson(CompletableFuture<String> curr) {
        return curr
            .thenApply(body -> gson.fromJson(body, GeographicTopTracks.class))
            .thenApply(dto -> dto.getTracks() == null
                    ? new Track[0]
                    : dto.getTracks().getTrack()
            );
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
     * Removes entries from cacheJsonPages and cacheTracksPages for a given country.
     * All CFs in progress will be canceled.
     * Since each CF is created as a downstream from the previous one, then we just need to
     * cancel the first CF in progress and all dependent CFs will be canceled too.
     * Each CF will propagate cancellation to its direct downstream and so on.
     *
     * @param country
     */
    public void clearCacheAndCancelRequests(String country) {
        country = country.toLowerCase();
        List<CompletableFuture<String>> json = cacheJsonPages.get(country);
        if(json == null) return;
        json
            .stream()
            .filter(cf -> !cf.isDone())
            .findFirst()
            .ifPresent(cf -> cf.cancel(true));
        cacheJsonPages.remove(country);
        /*
         * CF<List<Tracks>> are continuations of previous already canceled CFs.
         * Thus these ones have been canceled too.
         */
        cacheTracksPages.remove(country);
    }

    static class JsonAndTracks {
        final List<CompletableFuture<String>> json;
        final List<CompletableFuture<Track[]>> tracks;

        public JsonAndTracks(List<CompletableFuture<String>> json, List<CompletableFuture<Track[]>> tracks) {
            this.json = json;
            this.tracks = tracks;
        }

        static JsonAndTracks of(List<CompletableFuture<String>> json, List<CompletableFuture<Track[]>> tracks) {
            return new JsonAndTracks(json, tracks);
        }
    }
}
