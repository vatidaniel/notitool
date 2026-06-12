# Configuration with Spring
```java
@Configuration
public class NotificationConfiguration {

    // use fcm service account resource
    @Bean
    GoogleCredentials googleCredential(PropertiesBean properties) {
        Resource fcmServiceAccountResource = properties.getFcmServiceAccountResource();
        try {
            if (fcmServiceAccountResource != null) {
                try (InputStream inputStream = fcmServiceAccountResource.getInputStream()) {
                    return GoogleCredentials.fromStream(inputStream);
                }
            }
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            log.error(e.getMessage());
            return GoogleCredentials.newBuilder().build();
        }
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }

    @Bean
    FcmMessageManager fcmMessageManager(FirebaseMessaging messaging) {
        // Or new FcmMessageManager(messaging, customFactory) to inject a custom FcmMessageFactory.
        return new FcmMessageManager(messaging);
    }

    @Bean
    FcmTopicManager fcmTopicManager(FirebaseMessaging messaging) {
        return new FcmTopicManager(messaging);
    }

}
```

Notes:
- The managers auto-batch large requests to FCM limits (500 tokens/multicast, 1000/subscribe), so you
  can pass arbitrarily large device lists to `sendMulticast` / `subscribe` / `unsubscribe`.
- For non-blocking sends use the `*Async` methods, which return `CompletableFuture`.
- `notitool` depends on `slf4j-api` only; provide an SLF4J binding (e.g. `logback-classic`, or Spring
  Boot's default) so the managers' log output is rendered.