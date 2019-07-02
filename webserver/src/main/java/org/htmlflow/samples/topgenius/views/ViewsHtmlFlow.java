package org.htmlflow.samples.topgenius.views;

import htmlflow.DynamicHtml;
import org.htmlflow.samples.topgenius.model.Track;
import org.xmlet.htmlapifaster.EnumFormmethodMethodType;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumRelType;
import org.xmlet.htmlapifaster.EnumTypeButtonType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

public class ViewsHtmlFlow {

    public static DynamicHtml<TopTracksContext> toptracks = DynamicHtml.view(ViewsHtmlFlow::toptracksTemplate);

    static void toptracksTemplate(DynamicHtml<TopTracksContext> view, TopTracksContext ctx) {
        view
            .html()
                .head()
                    .link()
                        .attrRel(EnumRelType.STYLESHEET)
                        .attrHref("/stylesheets/bootstrap.min.css")
                    .__()
                    .title().text("TopGenius.eu").__()
                .__()
                .body()
                    .div()
                        .attrClass("container")
                        .div()
                            .attrClass("jumbotron")
                            .p().attrClass("lead").a().attrHref("/").text("TopGenius.eu").__().__()
                            .hr().__()
                            .form().attrClass("form-inline")
                                .div().attrClass("form-group")
                                    .label().attrClass("col-form-label").text("Country:").__()
                                    .input()
                                        .attrClass("form-control")
                                        .attrType(EnumTypeInputType.TEXT)
                                        .attrName("country")
                                        .attrId("inputCountry")
                                        .attrValue(ctx.country)
                                    .__()
                                    .label().attrClass("col-form-label").text("Limit:").__()
                                    .input()
                                        .attrClass("form-control")
                                        .attrType(EnumTypeInputType.TEXT)
                                        .attrName("limit")
                                        .attrId("inputLimit")
                                        .attrValue(ctx.limit + "")
                                    .__()
                                .__()
                                .button()
                                    .attrFormmethod(EnumFormmethodMethodType.GET)
                                    .attrType(EnumTypeButtonType.SUBMIT)
                                    .attrClass("btn btn-primary")
                                    .attrId("buttonTopTracks")
                                    .text("Top Tracks")
                                .__()
                                .button()
                                    .attrFormmethod(EnumFormmethodMethodType.POST)
                                    .attrFormaction("/sessions/clear/htmlflow")
                                    .attrType(EnumTypeButtonType.SUBMIT)
                                    .attrClass("btn btn-primary")
                                    .attrId("buttonClearCache")
                                    .text("Clear Cache for " + ctx.country)
                                .__()
                            .__() // form
                            .br().__()
                            .dynamic(jumbo -> { if(!ctx.hasSession) { jumbo
                                .form()
                                    .attrClass("form-inline alert alert-warning")
                                    .attrMethod(EnumMethodType.POST)
                                    .attrAction("/sessions/init")
                                    .button()
                                        .attrType(EnumTypeButtonType.SUBMIT)
                                        .attrClass("btn btn-outline-secondary")
                                        .attrId("buttonAccept")
                                        .text("Accept")
                                    .__() // button
                                    .text("cookies to store a per-user cache of Last.fm. Otherwise, there is no cache.")
                                .__(); // form
                            }})
                        .__() // div Jumbotron
                        .p()
                            .strong().text("Server processing time:").__()
                            .text((currentTimeMillis() - ctx.begin) / 1000.0)
                            .text(" ms for ")
                            .text(ctx.country)
                        .__()// p
                        .table()
                            .attrClass("table")
                            .tr()
                                .th().text("Rank").__()
                                .th().text("Track").__()
                                .th().text("Listeners").__()
                            .__()
                            .dynamic(table -> {
                                int[] count = {1};
                                ctx.tracks.forEach(track -> {
                                    table
                                        .tr()
                                            .td().dynamic(td -> td.text(count[0]++)).__()
                                            .td()
                                                .dynamic(td -> td
                                                    .a()
                                                        .attrHref(track.getUrl())
                                                        .attrTarget("_blank")
                                                        .text(track.getName())
                                                    .__())
                                            .__()
                                            .td().dynamic(td -> td.text(track.getListeners())).__()
                                        .__();
                                });
                            })
                        .__()
                    .__()
                .__()
            .__();
    }

    public static TopTracksContext context(String country, int limit, boolean hasSession, Stream<Track> tracks, long begin) {
        return new TopTracksContext(country, limit, hasSession, tracks, begin);
    }

    public static class TopTracksContext {
        final String country;
        final int limit;
        private final boolean hasSession;
        final Stream<Track> tracks;
        private final long begin;

        public TopTracksContext(String country, int limit, boolean hasSession, Stream<Track> tracks, long begin) {
            this.country = country;
            this.limit = limit;
            this.hasSession = hasSession;
            this.tracks = tracks;
            this.begin = begin;
        }
    }
}
