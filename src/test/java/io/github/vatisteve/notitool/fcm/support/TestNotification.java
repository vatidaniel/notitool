package io.github.vatisteve.notitool.fcm.support;

import io.github.vatisteve.notitool.fcm.domain.FcmNotification;

import java.util.Collections;
import java.util.Map;

/**
 * Simple {@link FcmNotification} test fixture.
 */
public class TestNotification implements FcmNotification {

    private final String title;
    private final String body;
    private final Map<String, String> data;

    public TestNotification(String title, String body) {
        this(title, body, Collections.emptyMap());
    }

    public TestNotification(String title, String body, Map<String, String> data) {
        this.title = title;
        this.body = body;
        this.data = data;
    }

    public static TestNotification simple() {
        return new TestNotification("title", "body");
    }

    public static TestNotification withData(Map<String, String> data) {
        return new TestNotification("title", "body", data);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public String getIcon() {
        return "icon";
    }

    @Override
    public String getColor() {
        return "#ffffff";
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }
}
