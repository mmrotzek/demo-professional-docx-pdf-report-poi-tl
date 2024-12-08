package rocks.m2x.demo.service.msgraph;

import com.microsoft.graph.beta.serviceclient.GraphServiceClient;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ClientSecretCredential;

@Component
public class Ms365Client {
    /**
     * The client credentials flow requires that you request the
     * /.default scope, and pre-configure your permissions on the
     * app registration in Azure. An administrator must grant consent
     * to those permissions beforehand.
     */
    final String[] scopes = new String[]{"https://graph.microsoft.com/.default"};

    public GraphServiceClient getGraphServiceClient(ApplicationConfigurationProperties.PdfConversionConfig.PdfConversionGraphApiConfig properties) {
        ClientSecretCredential  credential = new ClientSecretCredentialBuilder()
                .tenantId(properties.getTenantId())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .build();

        return new GraphServiceClient(credential, scopes);
    }
}
