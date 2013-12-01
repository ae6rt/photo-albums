package org.petrovic.photos;

import com.google.gson.Gson;
import org.petrovic.photos.resources.Web;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Json {

    public static void serializeToFile(Object o, File f) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(f);
        } catch (IOException e) {
            throw new WebApplicationException(e, Web.response(500, String.format("Error opening resource file: %s", f)));
        }
        try {
            writer.write(new Gson().toJson(o));
        } catch (IOException e) {
            throw new WebApplicationException(e, Web.response(500, String.format("Error writing resource: %s", f)));
        }
        try {
            writer.close();
        } catch (IOException e) {
            //
        }
    }

    static public <T> T deserializeFromFile(File f, Class<T> clazz) {
        String s = Strings.readStringFromFile(f);
        return new Gson().fromJson(s, clazz);
    }
}
