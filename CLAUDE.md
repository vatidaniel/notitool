# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`notitool` is a Java 17 library (packaged as a Maven artifact, not an application) that provides
a provider-agnostic abstraction for sending push notifications, plus a concrete implementation
backed by Firebase Cloud Messaging (FCM). It has no `main` method and no Spring dependency —
consumers wire it into their own app (see the Spring wiring template below). It depends on
`firebase-admin` and `slf4j-api` (logging façade only; the consumer supplies a binding).

## Build commands

```bash
mvn clean package      # compile + build the jar
mvn clean install      # install to local ~/.m2 for use by other projects
mvn test               # run the JUnit 5 / Mockito suite
mvn -Dtest=FcmMessageManagerTest test   # run a single test class
```

Tests mock `FirebaseMessaging`; nothing talks to a real Firebase project. Surefire passes
`-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` so Mockito's inline mock maker
works on newer JDKs (the dev/CI JVM here is JDK 25, while compilation targets 17).

## Architecture

The codebase is split into two layers under `io.github.vatisteve.notitool`:

- **`design/`** — the provider-agnostic contract. Pure interfaces with no FCM/Firebase imports.
  - `design/domain/` — domain abstractions, all heavily generic:
    - `INotification` (marker), `IDevice<K, T extends IDeviceType>` (token type `K`),
      `ITopic<T>`, `IDeviceGroup`, `IDeviceType`, `ICorrespondent<T>`.
  - `design/application/` — the two service contracts:
    - `MessageManager<N, D, T>` — sync send / sendMulticast / sendToTopic / sendToDeviceGroup,
      plus async `sendAsync` / `sendMulticastAsync` / `sendToTopicAsync` returning `CompletableFuture`.
    - `MessageFactory<N, M, A, D, T>` — builds provider-specific message objects from a notification.
    - `TopicManager<T, D>` — subscribe / unsubscribe (sync) plus `subscribeAsync` / `unsubscribeAsync`.
    - `MessageManagementResponse`, `TopicManagementResponse` — response contracts exposing
      `getSuccessCount()` / `getFailureCount()` / `isSuccessful()` (no downcast needed to read counts).
  - `design/exceptions/NotificationException` — the single checked exception all sync operations throw.

- **`fcm/`** — the Firebase implementation of the `design` contracts.
  - `FcmConstants` — FCM platform limits (500/multicast, 1000/subscribe-request, etc.), used to batch.
  - `FcmMessageManager implements MessageManager<FcmNotification, FcmDevice, FcmTopic>` —
    wraps a `FirebaseMessaging`; the `FcmMessageFactory` is injectable via constructor (a convenience
    constructor supplies a default). Large multicasts are auto-chunked into `MAX_DEVICE_PER_MULTICAST`.
  - `FcmMessageFactory` — builds `Message` / `MulticastMessage` objects, with per-platform
    builders (Android / APNs / WebPush) selected by `FcmDeviceType`.
  - `FcmTopicManager` — auto-chunks subscribe/unsubscribe into `MAX_SUBSCRIBE_DEVICE_PER_REQUEST`.
  - `FcmMessageResponse`, `FcmTopicResponse` — both have a `merge(...)` used to aggregate chunked results.
  - `fcm/internal/ApiFutureAdapter` — bridges Firebase's `ApiFuture` to `CompletableFuture` for the async API.
  - `fcm/domain/` — FCM-specialized domain types: `FcmNotification` (defines `Android/Apns/Web`
    notification-data records; exposes `getData()` payload, `getPriority()`, `getTimeToLive()`,
    `getBadge()` with sensible defaults), `FcmDevice extends IDevice<String, FcmDeviceType>`, `FcmTopic`.

### Key conventions and behaviors

- **The `design` layer must stay free of `com.google.firebase` imports** — that separation is the
  whole point. New providers should be added as sibling packages to `fcm/` implementing the same
  `design` interfaces.
- **Inactive devices are skipped, not errored.** Send and `subscribe` filter on `device.isActive()`
  and return a zero response (or omit the token) rather than calling Firebase. **`unsubscribe` does
  NOT filter** — stale/inactive tokens still need to be cleaned up off a topic.
- **Requests are auto-batched to FCM limits.** `sendMulticast` chunks devices into 500s and
  `subscribe`/`unsubscribe` chunk tokens into 1000s (Guava `Lists.partition`), then aggregate the
  per-chunk counts via `merge(...)`. Empty (all-filtered) lists short-circuit without an FCM call,
  avoiding the `IllegalArgumentException` Firebase throws on empty token lists.
- **`sendToDeviceGroup` is intentionally unimplemented** — it throws `UnsupportedOperationException`
  because FCM device groups require the legacy HTTP v1 server API, not the Admin SDK.
- Sync ops catch `FirebaseMessagingException` and rethrow as `NotificationException`; async ops
  complete the returned future exceptionally instead.
- `FcmMessageFactory.createMessage` dispatches on `FcmDeviceType`; a `null` device type means
  "send to all platforms" (`allPlatformsMessageForDeviceToken`). All-platform and topic messages
  include Android + APNs + **WebPush** config plus the `getData()` payload.

## Consuming this library (Spring)

`src/main/resources/template/fcm-configuration-with-spring.md` is the canonical wiring example:
define `GoogleCredentials` → `FirebaseApp` → `FirebaseMessaging` beans, then construct
`FcmMessageManager` and `FcmTopicManager` from the `FirebaseMessaging` bean. Credentials come from
an FCM service-account resource, falling back to application-default credentials.
