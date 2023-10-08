package com.ahmedmq.zatca.spring.service;

import com.ahmedmq.zatca.ZatcaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ZatcaAutoConfigurationTest {

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class));


    @Test
    void testZatcaServiceAutoConfiguration() {
        this.contextRunner.withUserConfiguration(RestTemplateAutoConfiguration.class,
                        ZatcaAutoConfiguration.class)
                .run(context -> assertThat(context).hasSingleBean(DefaultZatcaService.class));
    }

    @Test
    void registerProperties() {
        this.contextRunner.withUserConfiguration(RestTemplateAutoConfiguration.class,
                        ZatcaAutoConfiguration.class)
                .withPropertyValues("zatca.base-url=http://zatca.test",
                        "zatca.api-version=V3")
                .run(context -> {
                    assertThat(context).hasSingleBean(ZatcaProperties.class);
                    ZatcaProperties zatcaProperties = context.getBean(ZatcaProperties.class);
                    assertThat(zatcaProperties.baseUrl()).isEqualTo("http://zatca.test");
                    assertThat(zatcaProperties.apiVersion()).isEqualTo("V3");
                });
    }
}
