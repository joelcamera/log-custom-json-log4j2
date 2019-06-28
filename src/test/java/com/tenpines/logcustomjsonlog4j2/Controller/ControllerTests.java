package com.tenpines.logcustomjsonlog4j2.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ControllerTests {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private MockMvc mockClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        mockClient = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    public void whenThePostHasAnEmptyJsonBody_returnsOk() throws Exception {
        mockClient.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new HashMap<>())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenThePostHasAJsonBodyWithFields_returnsOk() throws Exception {
        Map<String, String> requestBody = new HashMap<String, String>() {{
            put("hello", "world");
        }};

        mockClient.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(requestBody)))
                .andExpect(status().isOk());
    }

    @Test
    public void whenThePostHasNonJsonBody_returnsOk() throws Exception {
        mockClient.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private String json(Object o) throws IOException {
        return objectMapper.writeValueAsString(o);
    }
}
