package omsu.omsuts.bot.java.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import omsu.omsuts.bot.java.api.OmsutsWebSocketConnection;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
public class Application implements Runnable {
    public static final String SERVER_ENDPOINT = "wss://localhost:8443/connect";

    @Getter
    private ApplicationComponent applicationComponent;

    @Inject
    public OmsutsWebSocketConnection connection;



    public Application() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);
    }



    //DEBUG ONLY! Disable Certificate Validation
    private OkHttpClient buildInsecuredClient() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
            log.error("Failed to build insecure debug client, return new OkHttpClient()", e);
            return new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build();
        }

        return new OkHttpClient.Builder()
                .sslSocketFactory(sc.getSocketFactory())
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }



    @Override
    public void run() {
        //OkHttpClient client = new OkHttpClient();

        // DEBUG ONLY!
        OkHttpClient client = buildInsecuredClient();


        Request request = new Request.Builder()
                .url(SERVER_ENDPOINT)
                .build();
        WebSocketCall call = WebSocketCall.create(client, request);
        call.enqueue(connection);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }
}
