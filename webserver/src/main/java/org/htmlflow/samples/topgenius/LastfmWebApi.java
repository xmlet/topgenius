package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import org.htmlflow.samples.topgenius.model.GeographicTopTracks;
import org.htmlflow.samples.topgenius.model.GetTopTracks;
import org.htmlflow.samples.topgenius.model.Track;

import java.io.InputStream;
import java.io.InputStreamReader;
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

    private final Map<String, Pair<Long, List<CompletableFuture<Track[]>>>> countryCache;
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

    private CompletableFuture<GeographicTopTracks> geoTopTracks(String path){
        return req
            .get(path)
            .thenApply(HttpResponse::body)
            .thenApply(strm -> {
                InputStreamReader reader = new InputStreamReader(strm);
                return gson.fromJson(reader, GeographicTopTracks.class);
            });
    }

    private CompletableFuture<Track[]> geoTopTracksArray(String path){
        return geoTopTracks(path)
            .thenApply(dto -> dto.getTracks() == null
                    ? new Track[0]
                    : dto.getTracks().getTrack()
            );
    }

    public CompletableFuture<Track[]> countryTopTracks(String country, int page){
        return geoTopTracksArray(geoTopTracksPath(country, page));
    }

    /**
     * All requests are performed sequentially.
     * This is asynchronous and we do not wait for response completion.
     */
    public Stream<Track> countryTopTracks(String country){
        country = country != null ? country.toLowerCase() : "";
        var pair = countryCache.computeIfAbsent(country, this::createCountryTopTracks);
        long dur = currentTimeMillis() - pair.key;
        if(dur > TTL) {
            pair = createCountryTopTracks(country);
            countryCache.put(country, pair);
        }
        List<CompletableFuture<Track[]>> all = pair.val;
        return all
            .stream()
            .map(CompletableFuture::join)
            .takeWhile(arr -> arr.length != 0)
            .flatMap(Stream::of);

    }

    private Pair<Long, List<CompletableFuture<Track[]>>> createCountryTopTracks(String country) {
        // Waisting a first page request just to get the total number of pages
        int totalPages = geoTopTracks(geoTopTracksPath(country, 1))
            .join()
            .getTracks()
            .getAttr()
            .getTotalPages();
        /**
         * Since streams are lazy, then we collect it to force requests to be dispatched.
         * Requests to geoTopTracksArray are made sequentially.
         * Every request through geoTopTracksArray is performed on the
         * completion of the previous request to avoid concurrency and
         * do not exceed rate limits of Last.fm.
         */
        ArrayList<CompletableFuture<Track[]>> seed = new ArrayList<>();
        seed.add(completedFuture(null));
        ArrayList<CompletableFuture<Track[]>> cfs = IntStream
            .rangeClosed(1, totalPages)
            .mapToObj(page -> geoTopTracksPath(country, page))
            .reduce( // !!! DO NOT replace it by collect() !!!!!
                seed,
                (lst, url) -> { // Enforces sequential requests to not exceed rate limits
                    CompletableFuture<Track[]> prev = lst.get(lst.size() - 1);
                    lst.add(prev.thenCompose(__ -> geoTopTracksArray(url))); // Next request only performed on completion of the previous one
                    return lst;
                },
                (l1, l2) -> null); // Keep it as null supplier. No combinator since it is not processed in parallel
        cfs.remove(0); // Remove the seed that is an empty CF
        return Pair.of(currentTimeMillis(), cfs);
    }

    public CompletableFuture<Track[]> artistTopTracks(String artisMbid, int page){
        String path = String.format(LASTFM_ARTIST_TOP_TRACKS, artisMbid, page);
        return req
            .get(path)
            .thenApply(HttpResponse::body)
            .thenApply(body -> {
                InputStreamReader reader = new InputStreamReader(body);
                GetTopTracks dto = gson.fromJson(reader, GetTopTracks.class);
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
    public void onResponse(Consumer<HttpResponse<InputStream>> cons) {
        final AsyncRequest old = req;
        this.req = path -> old
            .get(path)
            .whenComplete((in, err) -> cons.accept(in));
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
        Pair<Long, List<CompletableFuture<Track[]>>> pair = countryCache.get(country);
        if(pair == null) return;
        pair.val
            .stream()
            .filter(cf -> !cf.isDone())
            .findFirst()
            .ifPresent(cf -> cf.cancel(true));
        countryCache.remove(country);
    }

    static class Pair<K, V> {
        final K key;
        final V val;

        public Pair(K key, V val) {
            this.key = key;
            this.val = val;
        }

        static <S, E> Pair<S, E> of(S key, E val) {
            return new Pair<>(key, val);
        }
    }
}
