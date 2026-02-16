package com.example.hello.functional.steps;

import com.example.hello.persona.HelloSpringbootApplication;
import com.example.hello.persona.kafka.CampaignEventProducer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

@CucumberContextConfiguration
@SpringBootTest(classes = HelloSpringbootApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfig {

    @MockBean
    private CampaignEventProducer eventProducer;
}
