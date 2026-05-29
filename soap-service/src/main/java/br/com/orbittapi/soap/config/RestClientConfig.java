package br.com.orbittapi.soap.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SoapClientProperties.class)
public class RestClientConfig {

    @Bean(name = "satelliteHttpClient")
    public RestClient satelliteHttpClient(SoapClientProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory((ClientHttpRequestFactory) factory)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
