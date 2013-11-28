package org.petrovic.photos;

public class Strings {
    public static String extension(String imageFileName) {
        return imageFileName.substring(imageFileName.lastIndexOf(".") + 1);
    }

    public static String nameLessExtension(String s) {
        return s.substring(0, s.lastIndexOf("."));
    }
}
