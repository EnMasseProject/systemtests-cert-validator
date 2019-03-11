package io.messaging.ocp.enmasse;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class ConsoleConnect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public Future<Void> testConnection(Vertx vertx, JsonObject opts, File caCert) {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setSsl(true)
                .setTrustAll(false)
                .setPemTrustOptions(new PemTrustOptions()
                        .addCertPath(caCert.getAbsolutePath()))
                .setVerifyHost(false));
        
        logger.info("console connecting");
		Future<Void> resultPromise = Future.future();
        webClient.get(opts.getInteger("consolePort"), opts.getString("consoleHost"), "").ssl(true)
        .send(ar->{
            if (ar.succeeded()) {
            	logger.info("console succeeded");
                //http status doesn't matter the point is to successfully stablish the connection
                resultPromise.complete();
            } else {
            	logger.error("console failed ", ar.cause());
            	resultPromise.fail(ar.cause());
            }
        });
        return resultPromise;
	}
	
}
