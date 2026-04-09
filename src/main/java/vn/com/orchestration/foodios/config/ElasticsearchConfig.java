package vn.com.orchestration.foodios.config;

import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
@EnableElasticsearchRepositories(basePackages = "vn.com.orchestration.foodios.repository.search")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final Environment environment;

    public ElasticsearchConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        String uris = environment.getRequiredProperty("spring.elasticsearch.uris");
        String username = environment.getProperty("spring.elasticsearch.username");
        String password = environment.getProperty("spring.elasticsearch.password");
        boolean insecureSsl = environment.getProperty("app.elasticsearch.ssl.insecure", Boolean.class, false);

        URI uri = URI.create(uris);
        String connectionTarget = getConnectionTarget(uri);
        boolean useBasicAuth = hasText(username) && hasText(password);

        if ("https".equalsIgnoreCase(uri.getScheme())) {
            ClientConfiguration.TerminalClientConfigurationBuilder builder = insecureSsl
                    ? ClientConfiguration.builder()
                    .connectedTo(connectionTarget)
                    .usingSsl(getSSLContext(), (hostname, session) -> true)
                    : ClientConfiguration.builder()
                    .connectedTo(connectionTarget)
                    .usingSsl();

            if (useBasicAuth) {
                builder = builder.withBasicAuth(username, password);
            }

            if (insecureSsl) {
                builder = builder.withClientConfigurer(Rest5Clients.ElasticsearchHttpClientConfigurationCallback.from(httpClientBuilder ->
                        httpClientBuilder.setConnectionManager(
                                PoolingAsyncClientConnectionManagerBuilder.create()
                                        .setTlsStrategy(ClientTlsStrategyBuilder.create()
                                                .setSslContext(getSSLContext())
                                                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                                .build())
                                        .build()
                        )
                ));
            }

            return builder.build();
        }

        ClientConfiguration.TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(connectionTarget);

        if (useBasicAuth) {
            builder = builder.withBasicAuth(username, password);
        }

        return builder.build();
    }

    private String getConnectionTarget(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        }
        return uri.getHost() + ":" + port;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private SSLContext getSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
