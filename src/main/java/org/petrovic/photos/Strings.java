package org.petrovic.photos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Strings {
    public static String extension(String imageFileName) {
        return imageFileName.substring(imageFileName.lastIndexOf(".") + 1);
    }

    public static String nameLessExtension(String s) {
        return s.substring(0, s.lastIndexOf("."));
    }

    public static String readStringFromFile(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    public static void writeStringToFile(String s, File f) throws IOException {
        FileWriter writer = new FileWriter(f);
        writer.write(s);
        writer.close();
    }
}
