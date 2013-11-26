package com.xoom.oss.fs;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.JerseyServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        FeatherCon server = new JerseyServerBuilder("com.xoom.oss.fs.resources", "/*").withPort(8080).build();
        server.start();
    }
}
