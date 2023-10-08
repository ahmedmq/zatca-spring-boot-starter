package com.ahmedmq.zatca;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zatca")
public record ZatcaProperties(String baseUrl,
        String apiVersion) {
}
