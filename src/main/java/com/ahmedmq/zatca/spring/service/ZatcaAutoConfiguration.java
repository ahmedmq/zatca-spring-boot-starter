package com.ahmedmq.zatca.spring.service;

import com.ahmedmq.zatca.ZatcaProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(ZatcaProperties.class)
public class ZatcaAutoConfiguration {

    @Bean
    public ZatcaService zatcaService(RestTemplateBuilder builder, ZatcaProperties zatcaProperties) {
        RestTemplate restTemplate = builder
                .rootUri(zatcaProperties.baseUrl())
                .errorHandler(new ZatcaClientErrorHandler()).build();

        return new DefaultZatcaService(restTemplate, zatcaProperties);
    }
}
