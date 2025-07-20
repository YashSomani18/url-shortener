package com.example.UrlShortner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
class UrlShortnerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
       
        assertNotNull(applicationContext);
    }

    @Test
    void applicationStartsSuccessfully() {
        UrlShortnerApplication application = new UrlShortnerApplication();
        assertNotNull(application);
    }

    @Test
    void mainMethodExists() {
        assertDoesNotThrow(() -> {
            UrlShortnerApplication.main(new String[]{});
        });
    }

    @Test
    void applicationContextContainsExpectedBeans() {
        assertTrue(applicationContext.containsBean("urlShortnerApplication"));
    }
}
