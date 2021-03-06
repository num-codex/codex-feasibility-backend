package de.numcodex.feasibility_gui_backend.service.query_executor.impl.direct;

import de.numcodex.feasibility_gui_backend.service.query_executor.BrokerClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for providing a {@link DirectBrokerClient} instance.
 */
@Lazy
@Configuration
public class DirectSpringConfig {

    @Value("${app.flare.baseUrl}")
    private String flareBaseUrl;

    @Qualifier("direct")
    @Bean
    public BrokerClient directBrokerClient(){
        return new DirectBrokerClient();
    }

    @Bean
    public DirectConnector directConnector(RestTemplate restTemplate){
        return new DirectConnector(restTemplate, flareBaseUrl);
    }

}
