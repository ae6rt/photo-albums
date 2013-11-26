package com.xoom.oss.fs.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.ServletConfiguration;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class StaticContentTest {

    private FeatherCon server;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testWithDefaultServlet() throws Exception {
        ServletConfiguration servletConfiguration = new ServletConfiguration.Builder()
                .withServletClass(DefaultServlet.class)
                .withInitParameter("resourceBase", new File(getClass().getResource("/docbase/hello.html").getFile()).getParentFile().getAbsolutePath())
                .withPathSpec("/*")
                .build();
        server = new FeatherCon.Builder()
                .withServletConfiguration(servletConfiguration)
                .withPort(0)
                .build();
        server.start();

        Client client = Client.create();
        WebResource resource = client.resource(String.format("http://localhost:%d/hello.html", server.getHttpPort()));
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        String entity = clientResponse.getEntity(String.class);
        System.out.println(entity);
    }
}
