package com.tenpines.logcustomjsonlog4j2.logs;

import org.apache.logging.log4j.message.Message;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomMessage implements Message {
    private static final String TYPE = "type";
    private static final String BODY = "body";

    private final Map<String, Object> requestBody;

    public CustomMessage(Map<String, Object> requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public String getFormattedMessage() {
        JSONObject jsonBody = new JSONObject(requestBody);
        JSONObject jsonToLog = new JSONObject(new HashMap<String, Object>() {{
            put(TYPE, "custom");
            put(BODY, jsonBody);
        }});

        return jsonToLog.toString();
    }

    @Override
    public String getFormat() {
        return requestBody.toString();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
