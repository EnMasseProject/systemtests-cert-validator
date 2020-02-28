package io.messaging.ocp.enmasse;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

/**
 * Hello world!
 */
public class App {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Vertx vertx;
    private File caCert;

    public App() {
        this.caCert = new File("/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt");
        try {
            logger.info("ca cert " + new String(Files.readAllBytes(caCert.toPath())));
        } catch (IOException e) {
            logger.error("Error loading ca.crt", e);
        }
        this.vertx = Vertx.vertx();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(request -> {
            if (request.method() == HttpMethod.GET) {
                getHandler(request);
            }
        });
        int port = 8080;
        httpServer.listen(port);
        logger.info("systemtests-cert-validator app listening on port: {}", port);
    }

    private void getHandler(HttpServerRequest request) {
        request.bodyHandler(handler -> {
            JsonObject json = handler.toJsonObject();
            logger.info("Incoming GET request: {}", json);

            Future<Void> messagingResult = new MessagingConnect().testConnection(vertx, json, caCert);
            Future<Void> mqttResult = new MqttConnect().testConnection(vertx, json, caCert);

            CompositeFuture allResults = CompositeFuture.all(messagingResult, mqttResult);

            allResults.setHandler(ar -> {
                JsonObject responseData = new JsonObject();
                if (ar.succeeded()) {
                    responseData.put("ok", true);
                } else {
                    responseData.put("ok", false);
                    responseData.put("error", ar.cause().getMessage());
                    logger.error("error processing ", ar.cause());
                }
                logger.info("Responding " + responseData.toString());
                HttpServerResponse response = request.response();
                response.setStatusCode(HttpURLConnection.HTTP_OK);
                response.headers().add("Content-Type", "application/json");
                response.end(responseData.toString());
            });

        });
    }

    public static void main(String[] args) {
        new App();
    }

}
