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
    private static final String LOGGER = "LOGGER";
    private static Logger logger = LogManager.getLogger(LOGGER);

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    void toLog(@RequestBody Map<String, Object> requestBody) {
        logger.info(new CustomMessage(requestBody));
    }
}
