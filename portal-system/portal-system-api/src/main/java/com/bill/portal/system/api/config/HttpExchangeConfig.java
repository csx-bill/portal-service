package com.bill.portal.system.api.config;

import com.bill.portal.system.api.RemoteUserClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(types = {RemoteUserClient.class})
public class HttpExchangeConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        String baseUrl = "http://127.0.0.1:" + serverPort + contextPath;
        return groups -> groups.forEachClient((group, builder) ->
                builder.baseUrl(baseUrl).build());
    }
}
