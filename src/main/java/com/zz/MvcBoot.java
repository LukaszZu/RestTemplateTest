package com.zz;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;

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
            return restTemplate.getForObject("/test2", TestClass.class);
        }

        public TestClass[] getTest3() {
            return restTemplate.getForObject("/test3", TestClass[].class);
        }

    }

    @Configuration
    class MapperConfig {

        @Bean
        public ObjectMapper mapper() {
            Function<InputStream, InputStream> f = x -> new PrefixSkipInputStream(x);

            ObjectMapper om = new ObjectMapper(new InputModificationJsonFactory(f));
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
            return om;
        }
    }


    class PrefixSkipInputStream extends FilterInputStream {
        public static final String DEFAULT_PREFIX = ")]}', ";
        public static final String GUAM_PREFIX = ")]}',\n";

        private boolean prefixHandled = false;
        private String pattern = DEFAULT_PREFIX;

        public PrefixSkipInputStream(InputStream inputStream, String pattern) {
            super(inputStream);
            Assert.notNull(pattern, "Pattern cannot be null");
            this.pattern = pattern;
        }

        protected PrefixSkipInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int read(byte[] bytes, int i, int i1) throws IOException {
            if (prefixHandled) {
                return super.read(bytes, i, i1);
            }

            byte[] patternBytes = pattern.getBytes();
            int readBytes = in.read(patternBytes);

            if (readBytes < pattern.length()) {
                in.reset();
                return super.read(bytes, i, i1);
            }

            prefixHandled = handlePrefix(patternBytes);
            return super.read(bytes, i, i1);
        }

        private boolean handlePrefix(byte[] p) throws IOException {
            if (!Arrays.equals(p, pattern.getBytes())) {
                in.reset();
            }
            return true;
        }

    }

    class InputModificationJsonFactory extends JsonFactory {

        private Function<InputStream, InputStream> factory;

        InputModificationJsonFactory(Function<InputStream, InputStream> functionalDecorators) {
            this.factory = functionalDecorators;
        }

        @Override
        public JsonParser createParser(InputStream in) throws IOException, JsonParseException {
            return super.createParser(factory.apply(in));
        }
    }

}
