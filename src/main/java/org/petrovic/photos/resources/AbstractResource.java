package org.petrovic.photos.resources;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.imgscalr.Scalr;
import org.petrovic.photos.AlbumMetadata;
import org.petrovic.photos.Json;
import org.petrovic.photos.PhotoMetadata;
import org.petrovic.photos.Strings;
import org.petrovic.photos.Web;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

public class AbstractResource {

    protected final File albumsDirectory = new File("albums");
    protected final File staticContent = new File("static");
    protected final File html = new File(staticContent, "html");
    protected final File partials = new File(html, "partials");
    protected final String metaFileSuffix = "meta.json";
    protected final FilenameFilter fileFilter = new FilenameFilter() {

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
    protected final FilenameFilter directoryFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return new File(file, s).isDirectory();
        }
    };

    protected void createThumbnail(File albumDirectory, String imageFileName, String thumbnailImageFileName) {
        File imageFile = new File(albumDirectory, imageFileName);
        if (!imageFile.exists()) {
            throw new WebApplicationException(Web.response(404, String.format("Resource not found: %s", imageFile)));
        }
        try {
            BufferedImage img = ImageIO.read(imageFile);
            BufferedImage thumbImg = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 100, 100, Scalr.OP_ANTIALIAS);
            File thumbNailImageFile = new File(albumDirectory, thumbnailImageFileName);
            ImageIO.write(thumbImg, "jpg", thumbNailImageFile);
            writeExif(imageFile);
        } catch (IOException e) {
            throw new WebApplicationException(e, Web.response(500, String.format("Error reading resource: %s", imageFile)));
        }
    }

    protected AlbumMetadata loadAlbumMetaData(String albumName) {
        File metadataFile = new File(new File(albumsDirectory, albumName), "meta.json");
        if (metadataFile.exists()) {
            return Json.deserializeFromFile(metadataFile, AlbumMetadata.class);
        } else {
            AlbumMetadata albumMetadata = new AlbumMetadata(albumName, "desc " + albumName);
            Json.serializeToFile(albumMetadata, metadataFile);
            return albumMetadata;
        }
    }

    protected void writeExif(File imageFile) {
        File metadataFile = new File(imageFile.getParentFile(), Strings.nameLessExtension(imageFile.getName()) + ".meta.json");
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
                    photoMetadata = new PhotoMetadata(Strings.nameLessExtension(imageFile.getName()), date.toString(), latitude, longitude);
                }
            }
            if (photoMetadata == null) {
                photoMetadata = new PhotoMetadata(Strings.nameLessExtension(imageFile.getName()), date.toString());
            }
            Json.serializeToFile(photoMetadata, metadataFile);
        } catch (ImageProcessingException e) {
            throw new WebApplicationException(Web.response(404, String.format("Resource not found: %s", imageFile)));
        } catch (IOException e) {
            throw new WebApplicationException(e, Web.response(500, String.format("Error reading resource: %s", imageFile)));
        }
    }
}
