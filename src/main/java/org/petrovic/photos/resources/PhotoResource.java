package org.petrovic.photos.resources;

import org.petrovic.photos.Json;
import org.petrovic.photos.PhotoMetadata;
import org.petrovic.photos.Stream;
import org.petrovic.photos.Strings;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;

@Path("/photo")
public class PhotoResource extends AbstractResource {

    @GET
    @Path("/{albumNumber: [0-9]+}/{imageFile: .*\\.[jJ][pP][eE]{0,1}[gG]$}")
    @Produces("image/jpeg")
    public StreamingOutput getImage(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName,
                                    @DefaultValue("false") @QueryParam("thumbnail") Boolean useThumbnail) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        File imageFile;
        if (useThumbnail) {
            String fileExtension = Strings.extension(imageFileName);
            String thumbnailImageFileName = String.format("%s-thumbnail.%s", Strings.nameLessExtension(imageFileName), fileExtension);
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
    @Path("/{albumNumber: [0-9]+}")
    public void addPhoto(@PathParam("albumNumber") Integer albumNumber, byte[] imageBytes) {
        throw new UnsupportedOperationException();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{albumNumber: [0-9]+}/{imageFile: .*\\.[jJ][pP][eE]{0,1}[gG]}/metadata")
    public String photoMetadata(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName) {
        File metadataFile = new File(new File(albumsDirectory, albumNumber.toString()), String.format("%s.%s", Strings.nameLessExtension(imageFileName), metaFileSuffix));
        return Strings.readStringFromFile(metadataFile);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{albumNumber: [0-9]+}/{imageFile: .*\\.[jJ][pP][eE]{0,1}[gG]}/metadata")
    public void updatePhotoMetadata(@PathParam("albumNumber") Integer albumNumber, @PathParam("imageFile") String imageFileName, PhotoMetadata photoMetadata) {
        File photoMetadataFile = new File(new File(albumsDirectory, albumNumber.toString()), String.format("%s.%s", Strings.nameLessExtension(imageFileName), metaFileSuffix));
        PhotoMetadata update = Json.deserializeFromFile(photoMetadataFile, PhotoMetadata.class);
        update.caption = photoMetadata.caption;
        Json.serializeToFile(update, photoMetadataFile);
    }
}
