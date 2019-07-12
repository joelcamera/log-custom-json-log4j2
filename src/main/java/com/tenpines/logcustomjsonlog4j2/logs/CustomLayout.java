package com.tenpines.logcustomjsonlog4j2.logs;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.nio.charset.Charset;

@Plugin(name = "CustomLayout", category = "Core", elementType = "layout", printObject = true)
public class CustomLayout extends AbstractStringLayout {

    private static final String DEFAULT_EOL = "\r\n";

    protected CustomLayout(Charset charset) {
        super(charset);
    }

    @PluginFactory
    public static CustomLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new CustomLayout(charset);
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        return logEvent.getMessage().getFormattedMessage() + DEFAULT_EOL;
    }
}
