package org.htmlflow.samples.topgenius.views;

import htmlflow.DynamicHtml;
import org.htmlflow.samples.topgenius.model.Track;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumRelType;
import org.xmlet.htmlapifaster.EnumTypeButtonType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.util.stream.Stream;

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
                            .p().attrClass("lead").text("TopGenius.eu").__()
                            .hr().__()
                            .form().attrClass("form-inline").attrMethod(EnumMethodType.GET)
                                .div().attrClass("form-group")
                                    .label().attrClass("col-form-label").text("Country:").__()
                                    .input()
                                        .attrClass("form-control")
                                        .attrType(EnumTypeInputType.TEXT)
                                        .attrName("country")
                                        .attrId("inputCountry")
                                        .attrValue(ctx.country)
                                    .__()
                                    .label().attrClass("col-form-label").text("Number of tracks:").__()
                                    .input()
                                        .attrClass("form-control")
                                        .attrType(EnumTypeInputType.TEXT)
                                        .attrName("limit")
                                        .attrId("inputLimit")
                                        .attrValue(ctx.limit + "")
                                    .__()
                                .__()
                                .button()
                                    .attrType(EnumTypeButtonType.SUBMIT)
                                    .attrClass("btn btn-primary")
                                    .attrId("buttonTopTracks")
                                    .text("Top Tracks")
                                .__()
                            .__() // form
                        .__() // div Jumbotron
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

    public static TopTracksContext context(String country, int limit, Stream<Track> tracks) {
        return new TopTracksContext(country, limit, tracks);
    }

    public static class TopTracksContext {
        final String country;
        final int limit;
        final Stream<Track> tracks;

        public TopTracksContext(String country, int limit, Stream<Track> tracks) {
            this.country = country;
            this.limit = limit;
            this.tracks = tracks;
        }
    }
}
