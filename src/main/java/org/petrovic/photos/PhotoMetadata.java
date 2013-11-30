package org.petrovic.photos;

public class PhotoMetadata {
    public String caption;
    public String originalTime;
    public String lat;
    public String lng;

    public PhotoMetadata() {
    }

    public PhotoMetadata(String caption, String originalTime, double lat, double lng) {
        this.caption = caption;
        this.originalTime = originalTime;
        this.lat = String.format("%f", lat);
        this.lng = String.format("%f", lng);
    }

    public PhotoMetadata(String caption, String originalTime) {
        this.caption = caption;
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
