package com.example.hello.common;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class OpenObserveLogAppender extends AppenderBase<ILoggingEvent> {

    private String url = "http://localhost:5080/api/default/logs/_json";
    private String username = "admin@example.com";
    private String password = "Complexpass#123";

    public void setUrl(String url) { this.url = url; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            String timestamp = Instant.ofEpochMilli(event.getTimeStamp()).toString();
            String message = escapeJson(event.getFormattedMessage());
            String level = event.getLevel().toString();
            String logger = escapeJson(event.getLoggerName());
            String thread = escapeJson(event.getThreadName());

            StringBuilder json = new StringBuilder();
            json.append("[{");
            json.append("\"_timestamp\":\"").append(timestamp).append("\",");
            json.append("\"level\":\"").append(level).append("\",");
            json.append("\"message\":\"").append(message).append("\",");
            json.append("\"logger\":\"").append(logger).append("\",");
            json.append("\"thread\":\"").append(thread).append("\"");

            IThrowableProxy tp = event.getThrowableProxy();
            if (tp != null) {
                String stacktrace = escapeJson(ThrowableProxyUtil.asString(tp));
                json.append(",\"exception\":\"").append(stacktrace).append("\"");
            }
            json.append("}]");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String auth = Base64.getEncoder().encodeToString(
                    (username + ":" + password).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to send log to OpenObserve: " + e.getMessage());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
