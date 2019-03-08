package io.messaging.ocp.enmasse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MqttConnect {

	public Future<Void> testConnection(Vertx vertx, JsonObject opts, File caCert) {

		Future<Void> resultPromise = Future.future();
		
		MqttConnectOptions mqttOptions = new MqttConnectOptions();
		try {
			mqttOptions.setSocketFactory(new SNISettingSSLSocketFactory(getSocketFactory(new FileInputStream(caCert)), opts.getString("mqttHost")));
		} catch (Exception e) {
			resultPromise.fail(e);
		}
		mqttOptions.setUserName(opts.getString("username"));
		mqttOptions.setPassword(opts.getString("password").toCharArray());

        String serverURI = String.format("ssl://%s:%s", opts.getString("mqttHost"), opts.getString("mqttPort"));
		
		vertx.executeBlocking(f->{
			try {
				IMqttClient mqttClient = new MqttClient(serverURI, UUID.randomUUID().toString(), new MemoryPersistence());
				mqttClient.connect(mqttOptions);
				f.complete();
			} catch (MqttException e) {
				f.fail(e);
			}
		}, ar->{
			if(ar.succeeded()) {
				resultPromise.complete();
			}else {
				resultPromise.fail(ar.cause());
			}
		});
		
        return resultPromise;
	}

    private SSLSocketFactory getSocketFactory(InputStream caCrtFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        
        Collection<? extends Certificate> certs = cf.generateCertificates(caCrtFile);
        
        int count = 1;
        for(Certificate cert : certs) {
        	caKs.setCertificateEntry("cert"+count, cert);
        	count++;
        }
        
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);
        
        SSLContext context = tryGetSSLContext("TLSv1.2", "TLSv1.1", "TLS", "TLSv1");
        context.init(null, tmf.getTrustManagers(), new SecureRandom());

        return context.getSocketFactory();
    }
    
    private SSLContext tryGetSSLContext(final String... protocols) throws NoSuchAlgorithmException {
        for (String protocol : protocols) {
            try {
                return SSLContext.getInstance(protocol);
            } catch (NoSuchAlgorithmException e) {
                // pass and try the next protocol in the list
            }
        }
        throw new NoSuchAlgorithmException(String.format("Could not create SSLContext with one of the requested protocols: %s",
                Arrays.toString(protocols)));
    }
	
    private static class SNISettingSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory socketFactory;

        private final List<SNIServerName> sniHostNames;

        SNISettingSSLSocketFactory(final SSLSocketFactory socketFactory,
                                   final String host) {
            this.socketFactory = socketFactory;
            this.sniHostNames = Collections.singletonList(new SNIHostName(host));
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return socketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return socketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException {
            return setHostnameParameter(socketFactory.createSocket(socket, host, port, autoClose));
        }

        private Socket setHostnameParameter(final Socket newSocket) {
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setServerNames(this.sniHostNames);
            ((SSLSocket) newSocket).setSSLParameters(sslParameters);
            return newSocket;
        }

        @Override
        public Socket createSocket(final Socket socket, final InputStream inputStream, final boolean b)
                throws IOException {
            return setHostnameParameter(socketFactory.createSocket(socket, inputStream, b));
        }

        @Override
        public Socket createSocket() throws IOException {
            return setHostnameParameter(socketFactory.createSocket());
        }

        @Override
        public Socket createSocket(final String s, final int i) throws IOException {
            return setHostnameParameter(socketFactory.createSocket(s, i));
        }

        @Override
        public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1)
                throws IOException {
            return setHostnameParameter(socketFactory.createSocket(s, i, inetAddress, i1));
        }

        @Override
        public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
            return setHostnameParameter(socketFactory.createSocket(inetAddress, i));
        }

        @Override
        public Socket createSocket(final InetAddress inetAddress,
                                   final int i,
                                   final InetAddress inetAddress1,
                                   final int i1) throws IOException {
            return setHostnameParameter(socketFactory.createSocket(inetAddress, i, inetAddress1, i1));
        }
    }

}
