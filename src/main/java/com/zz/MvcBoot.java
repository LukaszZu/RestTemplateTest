package com.zz;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
            return restTemplate.getForObject("/test", TestClass.class);
        }

        public TestClass getTest2() {
//            ResponseEntity<String> forEntity = restTemplate.getForEntity("/test2", String.class);
//            String body = forEntity.getBody();
//            System.out.println(body);
            return restTemplate.getForObject("/test2", TestClass.class);
        }
    }

    @Configuration
    class MapperConfig {

        @Bean
        public ObjectMapper mapper() {
            ObjectMapper om = new ObjectMapper(new MyMapperFactory());
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
            return om;
        }
    }


    class MyStream extends FilterInputStream {
        String pattern = "})),\n";

        protected MyStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int read(byte[] bytes, int i, int i1) throws IOException {
            byte[] p = pattern.getBytes();
            int read1 = in.read(p);
            if (read1 == pattern.length()) {
                if (!Arrays.equals(p, pattern.getBytes())) {
                    System.out.println("Prefix not matches");
                    in.reset();
                }
            }
            return super.read(bytes, i, i1);
        }

    }

    class MyMapperFactory extends JsonFactory {
        @Override
        public JsonParser createParser(InputStream in) throws IOException, JsonParseException {
            System.out.println("PAAAA");
            return super.createParser(new MyStream(in));
        }
    }

}
