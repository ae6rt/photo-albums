package com.xoom.oss.fs.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Path("/")
public class AlbumsResource {

    private final File albumsDirectory = new File("albums");

    private final FilenameFilter fileFilter = new FilenameFilter() {
        private boolean isImageFile(String path) {
            return path.endsWith("JPG");
        }

        @Override
        public boolean accept(File file, String s) {
            return new File(file, s).isFile() && isImageFile(s);
        }
    };

    private final FilenameFilter directoryFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return new File(file, s).isDirectory();
        }
    };

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index() throws IOException {
        return fileReader(new File("static/html", "index.html").getPath());
    }

    @GET
    @Produces("application/javascript")
    @Path("/{file: .*js$}")
    public String javascripts(@PathParam("file") String path) {
        File f = new File("static", path);
        return fileReader(f.getPath());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/albums")
    public List<String> listAlbums() {
        String[] list = albumsDirectory.list(directoryFilter);
        return Arrays.asList(list);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/albums/{albumNumber: [0-9]+}")
    public List<String> listAlbum(@PathParam("albumNumber") Integer albumNumber) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        String[] list = albumDirectory.list(fileFilter);
        return Arrays.asList(list);
    }

    @GET
    @Path("/albums/{albumNumber: [0-9]+}/{imageFile}")
    @Produces("image/jpeg")
    public StreamingOutput getImage(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName,
                                    @DefaultValue("false") @QueryParam("thumbnail") Boolean thumbnail) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        if (thumbnail) {
            String[] split = imageFileName.split(".JPG");
            imageFileName = String.format("%s-thumbnail.JPG", split[0]);
        }

        File imageFile = new File(albumDirectory, imageFileName);
        final InputStream inputStream;
        try {
            inputStream = new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", imageFile))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                final byte[] buffer = new byte[1024 * 16];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                output.flush();
            }
        };
    }

    @POST
    @Consumes("octet/stream")
    public void addImage() {
        throw new UnsupportedOperationException();
    }

    private String fileReader(String path) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", path))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
        BufferedReader br = new BufferedReader(fileReader);
        String s;
        StringBuilder sb = new StringBuilder();
        try {
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            fileReader.close();
            return sb.toString();
        } catch (IOException e) {
            Response r = Response.status(500).entity(new ErrorMessage(String.format("Error reading resource: %s", path))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
    }

    public static class ErrorMessage {
        public final String error;

        public ErrorMessage(String error) {
            this.error = error;
        }
    }
}
