package io.github.vatisteve.notitool.fcm.support;

import io.github.vatisteve.notitool.fcm.domain.FcmTopic;

/**
 * Simple {@link FcmTopic} test fixture.
 */
public record TestTopic(String identifier) implements FcmTopic {

    public static TestTopic of(String identifier) {
        return new TestTopic(identifier);
    }

    @Override
    public String getTopicIdentifier() {
        return identifier;
    }
}
