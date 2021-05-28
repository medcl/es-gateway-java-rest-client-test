import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class Main {
    public static void main(String[] args) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {

        //cd ~/gateway-1.2.0*/data/gateway/nodes/0/certs
        //keytool -importcert -noprompt -alias esgateway -file root.cert -keystore client.jks -storepass storepass
        String keystorePath = "~/data/gateway/nodes/0/certs/client.jks";
        String keyStorePass = "storepass";

        KeyStore truststore = KeyStore.getInstance("jks");
        try (InputStream is = Files.newInputStream(Paths.get(keystorePath))) {
            truststore.load(is, keyStorePass.toCharArray());
        }
        SSLContextBuilder sslBuilder = SSLContexts.custom()
                .loadTrustMaterial(truststore, null);
        final SSLContext sslContext = sslBuilder
                .build();

        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "GvS84wgBNxOKGUPXtqTd"));

        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 8000, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setSSLContext(sslContext)
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setSSLHostnameVerifier(AllowAllHostnameVerifier.INSTANCE);
                    }
                });


        RestHighLevelClient client = new RestHighLevelClient(builder);



        try {
            CreateIndexRequest request = new CreateIndexRequest("twitterrrs-3");
            request.settings(Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 2)
            );
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
//            DeleteIndexRequest request = new DeleteIndexRequest("twitterrrs");
//            AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
//            System.out.println("createIndexResponse.index() = " + createIndexResponse.index());
            //关闭资源
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();

    }

}
