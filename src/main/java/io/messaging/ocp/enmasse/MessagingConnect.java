package io.messaging.ocp.enmasse;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonClientOptions;
import io.vertx.proton.ProtonConnection;

public class MessagingConnect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Future<Void> testConnection(Vertx vertx, JsonObject opts, File caCert) {

        Future<Void> resultPromise = Future.future();

        ProtonClientOptions options = new ProtonClientOptions()
                .setSsl(true)
                .setHostnameVerificationAlgorithm("")
                .setPemTrustOptions(new PemTrustOptions()
                        .addCertPath(caCert.getAbsolutePath()))
                .setTrustAll(false);

        logger.info("messaging connecting");
        ProtonClient proton = ProtonClient.create(vertx);
        proton.connect(options, opts.getString("messagingHost"), opts.getInteger("messagingPort"), opts.getString("username"), opts.getString("password"), connection -> {
            if (connection.succeeded()) {
                ProtonConnection conn = connection.result();
                conn.openHandler(result -> {
                    if (result.failed()) {
                        conn.close();
                        resultPromise.fail(result.cause());
                        logger.info("messaging failed opening");
                    } else {
                        logger.info("messaging succeeded");
                        resultPromise.complete();
                    }
                });
                conn.closeHandler(result -> {
                    if (result.failed()) {
                        conn.close();
                        resultPromise.fail(result.cause());
                        logger.info("messaging failed closing");
                    } else {
                        logger.info("messaging succeeded");
                        resultPromise.complete();
                    }
                });
                logger.info("messaging openning");
                conn.open();
            } else {
                logger.info("messaging failed connecting");
                resultPromise.fail(connection.cause());
            }
        });

        return resultPromise;
    }

}
