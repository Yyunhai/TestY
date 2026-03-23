package com.ysalu.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationLogService {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger("TESTY.APPLICATION");
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("TESTY.SECURITY");
    private static final Logger OPERATION_LOGGER = LoggerFactory.getLogger("TESTY.OPERATION");

    public void info(String event, String message, Object... fields) {
        APPLICATION_LOGGER.info(format(event, message, fields));
    }

    public void warn(String event, String message, Object... fields) {
        APPLICATION_LOGGER.warn(format(event, message, fields));
    }

    public void error(String event, String message, Throwable throwable, Object... fields) {
        APPLICATION_LOGGER.error(format(event, message, fields), throwable);
    }

    public void security(boolean success, String event, String message, Object... fields) {
        if (success) {
            SECURITY_LOGGER.info(format(event, message, fields));
            return;
        }
        SECURITY_LOGGER.warn(format(event, message, fields));
    }

    public void operation(boolean success, String event, String message, Object... fields) {
        if (success) {
            OPERATION_LOGGER.info(format(event, message, fields));
            return;
        }
        OPERATION_LOGGER.warn(format(event, message, fields));
    }

    private String format(String event, String message, Object... fields) {
        StringBuilder builder = new StringBuilder();
        builder.append("event=").append(sanitize(event));
        builder.append(" | message=").append(sanitize(message));
        if (fields == null || fields.length == 0) {
            return builder.toString();
        }
        for (int index = 0; index + 1 < fields.length; index += 2) {
            builder.append(" | ")
                    .append(sanitize(fields[index]))
                    .append('=')
                    .append(sanitize(fields[index + 1]));
        }
        return builder.toString();
    }

    private String sanitize(Object value) {
        if (value == null) {
            return "-";
        }
        String normalized = String.valueOf(value).replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.isEmpty() ? "-" : normalized;
    }
}
