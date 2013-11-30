package org.petrovic.photos;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Json {

    public static void serializeToFile(Object o, File f) throws IOException {
        FileWriter writer = new FileWriter(f);
        writer.write(new Gson().toJson(o));
        writer.close();
    }

    static public <T> T deserializeFromFile(File f, Class<T> clazz) throws IOException {
        String s = Strings.readStringFromFile(f);
        return new Gson().fromJson(s, clazz);
    }
}
