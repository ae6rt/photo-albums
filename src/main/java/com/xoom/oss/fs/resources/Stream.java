package com.xoom.oss.fs.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Stream implements StreamingOutput {

    private final File file;
    private final InputStream inputStream;

    public Stream(File file) {
        this.file = file;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Response r = Response.status(404).entity(new ErrorMessage(String.format("Resource not found: %s", file))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, len);
                output.flush();
            }
        } catch (IOException e) {
            Response r = Response.status(500).entity(new ErrorMessage(String.format("Error reading resource: %s", file))).header("Content-type", "application/json").build();
            throw new WebApplicationException(r);
        }
    }
}
