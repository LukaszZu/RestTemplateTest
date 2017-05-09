package com.zz;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zulk on 21.11.16.
 */
@SpringBootApplication
public class MvcBoot {

    public static void main(String[] args) {
        SpringApplication.run(MvcBoot.class, args);
    }

    @Service
    class ServiceClient {

        private final RestTemplate restTemplate;

        public ServiceClient(RestTemplateBuilder restTemplateBuilder) {
            restTemplate = restTemplateBuilder
                    .build();
        }

        public TestClass getTest() {
            return restTemplate.getForObject("/test",
                    TestClass.class);
        }
    }

    @Configuration
    class MapperConfig {

        @Bean
        public ObjectMapper mapper() {
            ObjectMapper om = new ObjectMapper();
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
            return om;
        }
    }
}
