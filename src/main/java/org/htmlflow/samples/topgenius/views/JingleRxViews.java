package org.htmlflow.samples.topgenius.views;

import htmlflow.DynamicHtml;
import org.htmlflow.samples.topgenius.model.Track;
import org.xmlet.htmlapifaster.EnumRelType;

import java.util.stream.Stream;

public class JingleRxViews {

    public static DynamicHtml<Stream<Track>> artists = DynamicHtml.view(JingleRxViews::artistsTemplate);

    static void artistsTemplate(DynamicHtml<Stream<Track>> view, Stream<Track> tracks) {
        view
            .html()
                .head()
                    .link()
                        .attrRel(EnumRelType.STYLESHEET)
                        .attrHref("/stylesheets/bootstrap.min.css")
                    .__()
                    .title().text("jingle").__()
                .__()
                .body()
                    .div()
                        .attrClass("container")
                        .div()
                            .attrClass("jumbotron")
                            .p().attrClass("lead").text("jingle").__()
                            .hr().__()
                        .__()
                        .table()
                            .attrClass("table")
                            .tr()
                                .th().text("Track").__()
                                .th().text("Listeners").__()
                                .th().text("Playcount").__()
                            .__()
                            .dynamic(table -> {
                                tracks.forEach(track -> {
                                    table
                                        .tr()
                                            .td()
                                                .dynamic(td -> td
                                                    .a()
                                                        .attrHref(track.getUrl())
                                                        .text(track.getName())
                                                    .__())
                                            .__()
                                            .td().dynamic(td -> td.text(track.getListeners())).__()
                                            .td().dynamic(td -> td.text(track.getPlaycount())).__()
                                        .__();
                                });
                            })
                        .__()
                    .__()
                .__()
            .__();
    }
}
