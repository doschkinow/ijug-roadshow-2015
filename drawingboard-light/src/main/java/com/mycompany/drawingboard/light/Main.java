package com.mycompany.drawingboard.light;

import grizzly.MyGrizzlyServerContainer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import javax.websocket.DeploymentException;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.tyrus.server.TyrusServerContainer;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/drawingboard/api/";

    /**
     * Creates and configures a Grizzly HTTP server as a Jersey container, 
     * exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer getJerseyContainer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.mycompany.drawingboard.light package
        final ResourceConfig rc = new ResourceConfig()
                .register(DrawingsResource.class)
                .register(SseFeature.class)
                .register(MoxyJsonFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/main/resources/"), "/drawingboard");
        //com.sun.net.httpserver.HttpServer server = JdkHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);

        return server;
    }
    
    /**
     * Creates and configures a Tyrus server container, based on an existing grizzly HTTP server
     * @return Tyrus server container.
     */
    public static TyrusServerContainer getTyrusContainer(HttpServer server) throws DeploymentException {
        TyrusServerContainer container = (TyrusServerContainer)new MyGrizzlyServerContainer(server).createContainer(null);
        container.addEndpoint(DrawingWebSocket.class);
        return container;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DeploymentException {
        final HttpServer jcontainer = getJerseyContainer();
        TyrusServerContainer tcontainer = getTyrusContainer(jcontainer);
        
        tcontainer.start("/drawingboard", 8080);
        System.out.println(String.format("drawingboard-light started at %s"  + 
             "\nHit enter to stop it...", BASE_URI.substring(0, BASE_URI.indexOf("api"))));
        System.in.read();
        tcontainer.stop();
    }
}

