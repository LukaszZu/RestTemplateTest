package com.zz;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.zz.MvcBoot.PrefixSkipInputStream.DEFAULT_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
/**
 * Created by zulk on 21.11.16.
 */
@RunWith(SpringRunner.class)
@RestClientTest(MvcBoot.ServiceClient.class)
public class TemplateTest {

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    ApplicationContext ctx;

    @Autowired
    private MvcBoot.ServiceClient service;

    String jsonString;
    String bigJson;

    @Before
    public void before() throws IOException {
        jsonString = String.join("\n", Files.readAllLines(ctx.getResource("test.json").getFile().toPath()));

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES).build();
        TestClass testClass = mapper.readValue(jsonString, TestClass.class);
        List<TestClass> testClassList = IntStream.range(0, 1000).boxed().map(f -> testClass).collect(Collectors.toList());

        bigJson = mapper.writeValueAsString(testClassList);
        System.out.println(bigJson);
    }

    @Test
    public void jsonShouldBEParsedPropely() throws IOException {
        //given

        server.expect(requestTo("/test")).andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        //when
        TestClass tc = service.getTest();

        //then
        assertThat(tc.getTotal(),equalTo(123456));
        assertThat(tc.getItems().size(),equalTo(2));
        assertThat(tc.getItems(),contains(items()));
    }

    @Test
    public void prefixShouldBeRemoved() throws IOException {
        server.expect(requestTo("/test2")).andRespond(withSuccess(DEFAULT_PREFIX + jsonString, MediaType.APPLICATION_JSON));
        TestClass tc = service.getTest2();
        //then
        assertThat(tc.getTotal(), equalTo(123456));
        assertThat(tc.getItems().size(), equalTo(2));
        assertThat(tc.getItems(), contains(items()));
    }

    @Test
    public void shouldRemovePrefixWhenBigInput() throws IOException {
        server.expect(requestTo("/test3")).andRespond(withSuccess(DEFAULT_PREFIX + bigJson, MediaType.APPLICATION_JSON));
        TestClass[] tc = service.getTest3();
        //then
        assertThat(tc.length, equalTo(1000));
    }

    @Test
    public void shouldParseWhenBigInput() throws IOException {
        server.expect(requestTo("/test3")).andRespond(withSuccess(bigJson, MediaType.APPLICATION_JSON));
        TestClass[] tc = service.getTest3();
        //then
        assertThat(tc.length, equalTo(1000));
    }



    private Item[] items() {
        return ImmutableList.of(new Item(111),new Item(222)).toArray(new Item[0]);
    }
}
