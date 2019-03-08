package io.messaging.ocp.enmasse;

import java.io.File;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class ConsoleConnect {

	public Future<Void> testConnection(Vertx vertx, JsonObject opts, File caCert) {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setSsl(true)
                .setTrustAll(false)
                .setPemTrustOptions(new PemTrustOptions()
                        .addCertPath(caCert.getAbsolutePath()))
                .setVerifyHost(false));
        
		Future<Void> resultPromise = Future.future();
        webClient.get(opts.getInteger("consolePort"), opts.getString("consoleHost"), "").ssl(true)
        .send(ar->{
            if (ar.succeeded()) {
                //http status doesn't matter the point is to successfully stablish the connection
                resultPromise.complete();
            } else {
            	resultPromise.fail(ar.cause());
            }
        });
        return resultPromise;
	}
	
}
