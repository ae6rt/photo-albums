package org.petrovic.photos;

import org.petrovic.photos.ErrorMessage;

import javax.ws.rs.core.Response;

public class Web {

    public static Response response(int code, String message) {
        return Response.status(code).entity(new ErrorMessage(message)).header("Content-type", "application/json").build();
    }
}
