package de.numcodex.feasibility_gui_backend.service;

import de.numcodex.feasibility_gui_backend.repository.ResultRepository;
import de.numcodex.feasibility_gui_backend.service.query_executor.BrokerClient;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryStatusListenerImpl;
import de.numcodex.feasibility_gui_backend.service.query_executor.impl.mock.MockBrokerClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class BrokerClientProducer {

  public static final String CLIENT_TYPE_DSF = "DSF";
  public static final String CLIENT_TYPE_AKTIN = "AKTIN";
  public static final String CLIENT_TYPE_MOCK = "MOCK";

  private final ResultRepository resultRepository;

  /*
    //TODO: integrate when DSFBrokerClient can be autowired safely
    @Autowired
    @Qualifier("dsf")
  */
  private BrokerClient dsfBrokerClient;

  @Autowired
  @Qualifier("aktin")
  private BrokerClient aktinBrokerClient;

  public BrokerClientProducer(@Autowired ResultRepository resultRepository) {
    this.resultRepository = resultRepository;
  }

  @Qualifier("applied")
  @Bean
  public BrokerClient createBrokerClient(@Value("${app.broker-client}") String type) throws IOException {
    BrokerClient brokerClient = null;

    if (StringUtils.equalsIgnoreCase(type, CLIENT_TYPE_DSF)) {
      brokerClient = dsfBrokerClient;
    }

    if (StringUtils.equalsIgnoreCase(type, CLIENT_TYPE_AKTIN)) {
      brokerClient = aktinBrokerClient;
    }

    if (StringUtils.equalsIgnoreCase(type, CLIENT_TYPE_MOCK)) {
      brokerClient = new MockBrokerClient();
    }

    if (brokerClient == null) {
      throw new IllegalStateException(
          String.format(
              "No Broker Client configured for type '%s'. Allowed types are %s",
              type, List.of(CLIENT_TYPE_DSF, CLIENT_TYPE_AKTIN, CLIENT_TYPE_MOCK)));
    }

    brokerClient.addQueryStatusListener(
        new QueryStatusListenerImpl(this.resultRepository, brokerClient));

    return brokerClient;
  }
}
