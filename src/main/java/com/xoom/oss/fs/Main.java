package com.xoom.oss.fs;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.JerseyServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        FeatherCon server = new JerseyServerBuilder("com.xoom.oss.fs.resources", "/api/*").withPort(19060).build();
        server.start();
    }
}
