package watchtower.workflow.camunda.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchtowerWorkflowCamundaConfiguration {
  private static final Logger logger = LoggerFactory
      .getLogger(WatchtowerWorkflowCamundaConfiguration.class);

  private static WatchtowerWorkflowCamundaConfiguration instance = null;

  private String endpoint = null;

  public WatchtowerWorkflowCamundaConfiguration() {
    Properties properties = new Properties();
    InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream("configuration.properties");

    if (inputStream != null) {
      try {
        properties.load(inputStream);
      } catch (IOException e) {
        logger.error("Failed to load configuration");
      }

      endpoint = properties.getProperty("endpoint");
    } else {
      logger.error("Property file configuration.properties not found in the classpath");
    }
  }

  public String getEndpoint() {
    return endpoint;
  }

  public static WatchtowerWorkflowCamundaConfiguration getInstance() {
    if (instance == null)
      instance = new WatchtowerWorkflowCamundaConfiguration();

    return instance;
  }
}