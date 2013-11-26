package com.xoom.oss.fs.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.ServletConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Ignore
public class ServletInstanceTest {
    private FeatherCon server;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testWithServletInstance() throws Exception {
        ServletConfiguration servletConfiguration = new ServletConfiguration.Builder()
                .withServlet(new HttpServlet() {
                    @Override
                    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                        resp.getWriter().println("hello client");
                    }
                })
                .withPathSpec("/*")
                .build();
        server = new FeatherCon.Builder()
                .withServletConfiguration(servletConfiguration)
                .withPort(0)
                .build();
        server.start();

        Client client = Client.create();
        WebResource resource = client.resource(String.format("http://localhost:%d/anything", server.getHttpPort()));
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        String entity = clientResponse.getEntity(String.class);
        System.out.println(entity);
    }
}
