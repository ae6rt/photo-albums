package com.xoom.oss.fs.resources;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.google.gson.Gson;
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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
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
    @Produces(MediaType.TEXT_HTML)
    @Path("/partials/{file: .*html$}")
    public String html(@PathParam("file") String path) {
        return plainTextFromFile(new File("static/html/partials", path));
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
        return new Stream(imageFile);
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void addImage() {
        throw new UnsupportedOperationException();
    }

    private void createThumbnail(File albumDirectory, String imageFileName, String thumbnailImageFileName) {
        File imageFile = new File(albumDirectory, imageFileName);
        if (!imageFile.exists()) {
            throw new WebApplicationException(response(404, String.format("Resource not found: %s", imageFile)));
        }
        try {
            BufferedImage img = ImageIO.read(imageFile);
            BufferedImage thumbImg = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 100, 100, Scalr.OP_ANTIALIAS);
            File thumbNailImageFile = new File(albumDirectory, thumbnailImageFileName);
            ImageIO.write(thumbImg, "jpg", thumbNailImageFile);
            writeExif(imageFile);
        } catch (IOException e) {
            throw new WebApplicationException(e, response(500, String.format("Error reading resource: %s", imageFile)));
        }
    }

    private void writeExif(File imageFile) {
        File metadataFile = new File(imageFile.getParentFile(), imageFile.getName().split(".JPG")[0] + ".meta");
        try {
            PhotoMetadata photoMetadata = null;
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            Date date = null;
            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof ExifSubIFDDirectory) {
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                }
                if (directory instanceof GpsDirectory) {
                    GpsDirectory gpsDirectory = (GpsDirectory) directory;
                    GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                    double latitude = geoLocation.getLatitude();
                    double longitude = geoLocation.getLongitude();
                    photoMetadata = new PhotoMetadata(date.toString(), latitude, longitude);
                }
            }
            if (photoMetadata == null) {
                photoMetadata = new PhotoMetadata(date.toString());
            }
            FileWriter fileWriter = new FileWriter(metadataFile);
            fileWriter.write(new Gson().toJson(photoMetadata));
            fileWriter.close();
        } catch (ImageProcessingException e) {
            throw new WebApplicationException(response(404, String.format("Resource not found: %s", imageFile)));
        } catch (IOException e) {
            throw new WebApplicationException(e, response(500, String.format("Error reading resource: %s", imageFile)));
        }
    }

    private class PhotoMetadata {
        public String originalTime;
        public String lat;
        public String lng;

        public PhotoMetadata() {
        }

        public PhotoMetadata(String originalTime, double lat, double lng) {
            this.originalTime = originalTime;
            this.lat = String.format("%f", lat);
            this.lng = String.format("%f", lng);
        }

        public PhotoMetadata(String originalTime) {
            this.originalTime = originalTime;
            this.lat = "";
            this.lng = "";
        }

        @Override
        public String toString() {
            return "PhotoMetadata{" +
                    "originalTime='" + originalTime + '\'' +
                    ", lat='" + lat + '\'' +
                    ", lng='" + lng + '\'' +
                    '}';
        }
    }

    private String plainTextFromFile(File file) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(response(404, String.format("Resource not found: %s", file)));
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
            throw new WebApplicationException(response(500, String.format("Error reading resource: %s", file)));
        }
    }

    private Response response(int code, String message) {
        return Response.status(code).entity(new ErrorMessage(message)).header("Content-type", "application/json").build();
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
