package org.petrovic.photos.resources;

import org.petrovic.photos.Json;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/albums")
public class AlbumsResource extends AbstractResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AlbumMetadata> listAlbums() {
        String[] albumDirectoryNames = albumsDirectory.list(directoryFilter);
        List<AlbumMetadata> albumMetadatas = new ArrayList<AlbumMetadata>();
        for (String albumName : albumDirectoryNames) {
            AlbumMetadata albumMetadata = loadAlbumMetaData(albumName);
            albumMetadatas.add(albumMetadata);
        }
        return albumMetadatas;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{albumNumber: [0-9]+}/photos")
    public List<String> listAlbumPhotos(@PathParam("albumNumber") Integer albumNumber) {
        File albumDirectory = new File(albumsDirectory, albumNumber.toString());
        String[] list = albumDirectory.list(fileFilter);
        return Arrays.asList(list);
    }

    @PUT
    @Path("/{albumNumber: [0-9]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAlbumMetadata(@PathParam("albumNumber") Integer albumNumber, AlbumMetadata albumMetadata) {
        File metadataFile = new File(new File(albumsDirectory, albumNumber.toString()), metaFileSuffix);
        Json.serializeToFile(albumMetadata, metadataFile);
    }
}
