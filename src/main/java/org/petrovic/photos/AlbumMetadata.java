package org.petrovic.photos;

public class AlbumMetadata {
    public String name;
    public String description;

    public AlbumMetadata() {
    }

    public AlbumMetadata(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "AlbumMetadata{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
