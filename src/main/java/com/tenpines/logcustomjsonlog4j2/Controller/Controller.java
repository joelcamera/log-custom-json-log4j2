package com.tenpines.logcustomjsonlog4j2.Controller;

import com.tenpines.logcustomjsonlog4j2.logs.CustomMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class Controller {
    private static final String LOGGER_WITH_CUSTOM_LAYOUT = "LOGGER_WITH_CUSTOM_LAYOUT";
    private static Logger loggerWithCustomLayout = LogManager.getLogger(LOGGER_WITH_CUSTOM_LAYOUT);
    private static final String LOGGER_WITH_JSON_LAYOUT = "LOGGER_WITH_JSON_LAYOUT";
    private static Logger loggerWithJsonLayout = LogManager.getLogger(LOGGER_WITH_JSON_LAYOUT);

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    void toLogWithTheCustomLayout(@RequestBody Map<String, Object> requestBody) {
        loggerWithCustomLayout.info(new CustomMessage(requestBody));
    }

    @PostMapping("/json-layout")
    @ResponseStatus(HttpStatus.OK)
    void toLogWithTheJsonLayout(@RequestBody Map<String, Object> requestBody) {
        loggerWithJsonLayout.info(requestBody);
    }
}
