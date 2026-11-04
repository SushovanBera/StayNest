package com.staynest.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credential.path:classpath:firebase-service-account.json}")
    private Resource credentialResource;

    @Value("${firebase.project-id:staynest-booking}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (credentialResource == null || !credentialResource.exists()) {
                    logger.warn("Firebase credential file not found at {}. Firebase initialization skipped.", credentialResource);
                    return;
                }

                try (InputStream serviceAccount = credentialResource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setProjectId(projectId)
                            .build();

                    FirebaseApp.initializeApp(options);
                    logger.info("Successfully connected to Firebase project: {}", projectId);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }

    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("FirebaseApp not initialized. Firestore client bean may be null or unavailable.");
            return null;
        }
        try {
            return FirestoreClient.getFirestore();
        } catch (Throwable t) {
            logger.error("Could not initialize Firestore client: {}", t.getMessage());
            return null;
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }
        try {
            return FirebaseAuth.getInstance();
        } catch (Throwable t) {
            logger.warn("Could not initialize FirebaseAuth client: {}", t.getMessage());
            return null;
        }
    }
}
