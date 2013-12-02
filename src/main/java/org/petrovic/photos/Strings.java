package org.petrovic.photos;

import javax.ws.rs.WebApplicationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

    public static String readStringFromFile(File f) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Web.response(404, String.format("Resource not found: %s", f)));
        }
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new WebApplicationException(Web.response(500, String.format("Error reading resource: %s", f)));
        }
        try {
            reader.close();
        } catch (IOException e) {
            // intentionally empty
        }
        return sb.toString();
    }

    public static void writeStringToFile(String s, File f) throws IOException {
        FileWriter writer = new FileWriter(f);
        writer.write(s);
        writer.close();
    }
}
