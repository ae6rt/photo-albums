package org.petrovic.photos.resources;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.google.gson.Gson;
import org.imgscalr.Scalr;
import org.petrovic.photos.ErrorMessage;
import org.petrovic.photos.PhotoMetadata;
import org.petrovic.photos.Stream;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Path("/")
public class AlbumsResource {

    private final File albumsDirectory = new File("albums");

    private final FilenameFilter fileFilter = new FilenameFilter() {

        private boolean hasImageExtension(String fileName) {
            String normalizedFileName = fileName.toLowerCase();
            return normalizedFileName.endsWith("jpg") || normalizedFileName.endsWith("png") || normalizedFileName.endsWith("gif");
        }

        private boolean isImageFile(String path) {
            return hasImageExtension(path) && !path.contains("-thumbnail");
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
    @Path("/albums/{albumNumber: [0-9]+}/{imageFile: .*\\.[jJ][pP][gG]$}")
    @Produces("image/jpeg")
    public StreamingOutput getImage(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName,
                                    @DefaultValue("false") @QueryParam("thumbnail") Boolean useThumbnail) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        File imageFile;
        if (useThumbnail) {
            String fileExtension = extension(imageFileName);
            String thumbnailImageFileName = String.format("%s-thumbnail.%s", nameLessExtension(imageFileName), fileExtension);
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

    private String extension(String imageFileName) {
        return imageFileName.substring(imageFileName.lastIndexOf(".") + 1);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/metadata/{albumNumber: [0-9]+}/{imageFile: .*\\.[jJ][pP][gG]$}")
    public String metadata(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName) {
        File t = new File(new File(albumsDirectory, albumNumber.toString()), nameLessExtension(imageFileName) + ".meta");
        return plainTextFromFile(t);
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

    private String nameLessExtension(String s) {
        return s.substring(0, s.lastIndexOf("."));
    }

    private void writeExif(File imageFile) {
        File metadataFile = new File(imageFile.getParentFile(), nameLessExtension(imageFile.getName()) + ".meta");
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

}
