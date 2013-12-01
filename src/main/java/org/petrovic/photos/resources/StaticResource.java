package org.petrovic.photos.resources;

import org.petrovic.photos.Stream;
import org.petrovic.photos.Strings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;

@Path("/")
public class StaticResource extends AbstractResource {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index() throws IOException {
        return Strings.readStringFromFile(new File(html, "index.html"));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/partials/{file: .*html$}")
    public String html(@PathParam("file") String partialViewFile) {
        return Strings.readStringFromFile(new File(partials, partialViewFile));
    }

    @GET
    @Produces("application/javascript")
    @Path("/{file: .*js(.map){0,1}$}")
    public String javascripts(@PathParam("file") String javaScriptFileName) {
        return Strings.readStringFromFile(new File(staticContent, javaScriptFileName));
    }

    @GET
    @Produces("text/css")
    @Path("/{file: .*css$}")
    public String css(@PathParam("file") String cssFilePath) {
        return Strings.readStringFromFile(new File(staticContent, cssFilePath));
    }

    @GET
    @Produces("image/png")
    @Path("/{file: .*png$}")
    public StreamingOutput png(@PathParam("file") String pngFilePath) {
        return new Stream(new File(staticContent, pngFilePath));
    }
}
