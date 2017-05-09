package com.zz;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
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

    @Before
    public void before() throws IOException {
        jsonString = String.join("\n", Files.readAllLines(ctx.getResource("test.json").getFile().toPath()));
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
        server.expect(requestTo("/test2")).andRespond(withSuccess("})),\n" + jsonString, MediaType.APPLICATION_JSON));
        TestClass test2 = service.getTest2();

        System.out.println(test2);
    }
    
    private Item[] items() {
        return ImmutableList.of(new Item(111),new Item(222)).toArray(new Item[0]);
    }
}
