package org.petrovic.photos;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.FilterWrapper;
import com.xoom.oss.feathercon.JerseyServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        FilterWrapper.Builder filterBuilder = new FilterWrapper.Builder();
        filterBuilder.withFilterClass(AccessLoggingFilter.class).withPathSpec("/*");
        FeatherCon server = new JerseyServerBuilder("org.petrovic.photos.resources", "/*")
                .withPort(8080)
                .withFilter(filterBuilder.build())
                .build();
        server.start();
    }
}
