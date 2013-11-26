package com.xoom.oss.fs.resources;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
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
import java.awt.image.BufferedImage;
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
            return path.endsWith("JPG") && !path.contains("-thumbnail");
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
        return plainTextFromFile(new File("static/html", "index.html"));
    }

    @GET
    @Produces("application/javascript")
    @Path("/{file: .*js$}")
    public String javascripts(@PathParam("file") String path) {
        return plainTextFromFile(new File("static", path));
    }

    @GET
    @Produces("text/css")
    @Path("/{file: .*css$}")
    public String css(@PathParam("file") String path) {
        return plainTextFromFile(new File("static", path));
    }

    @GET
    @Produces("image/png")
    @Path("/{file: .*png$}")
    public StreamingOutput png(@PathParam("file") String path) {
        return new Stream(new File("static", path));
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
    @Path("/albums/{albumNumber: [0-9]+}/{imageFile: .*JPG}")
    @Produces("image/jpeg")
    public StreamingOutput getImage(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName,
                                    @DefaultValue("false") @QueryParam("thumbnail") Boolean useThumbnail) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        File imageFile;
        if (useThumbnail) {
            String thumbnailImageFileName = String.format("%s-thumbnail.JPG", imageFileName.split(".JPG")[0]);
            File thumbnailFile = new File(albumDirectory, thumbnailImageFileName);
            if (!thumbnailFile.exists()) {
                createThumbnail(albumDirectory, imageFileName, thumbnailImageFileName);
            }
            imageFile = thumbnailFile;
        } else {
            imageFile = new File(albumDirectory, imageFileName);
        }
        System.out.printf("@@@ image: %s (thumbnail=%s)\n", imageFile, useThumbnail);
        return new Stream(imageFile);
    }

    @POST
    @Consumes("octet/stream")
    public void addImage() {
        throw new UnsupportedOperationException();
    }

    private void createThumbnail(File albumDirectory, String imageFileName, String thumbnailImageFileName) {
        File imageFile = new File(albumDirectory, imageFileName);
        if (!imageFile.exists()) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", imageFile))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
        try {
            BufferedImage img = ImageIO.read(imageFile);
            BufferedImage thumbImg = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 50, 50, Scalr.OP_ANTIALIAS);
            File thumbNailImageFile = new File(albumDirectory, thumbnailImageFileName);
            ImageIO.write(thumbImg, "jpg", thumbNailImageFile);
        } catch (IOException e) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", imageFile))).header("Content-type", "application/json").build();
            throw new WebApplicationException(e, r);
        }
    }

    private String plainTextFromFile(File file) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", file))).header("Content-type", "application/json").build();
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
            Response r = Response.status(500).entity(new ErrorMessage(String.format("Error reading resource: %s", file))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
    }

    public static class Stream implements StreamingOutput {

        private final File file;
        private final InputStream inputStream;

        public Stream(File file) {
            this.file = file;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", file))).header("Content-type", "application/json").build();
                throw new WebApplicationException(r);
            }
        }

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            try {
                byte[] buffer = new byte[1024 * 8];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                    output.flush();
                }
            } catch (IOException e) {
                Response r = Response.status(500).entity(new ErrorMessage(String.format("Error reading resource: %s", file))).header("Content-type", "application/json").build();
                throw new WebApplicationException(r);
            }
        }
    }

    public static class ErrorMessage {
        public final String error;

        public ErrorMessage(String error) {
            this.error = error;
        }
    }
}
