package com.staynest.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreService.class);

    private final Firestore firestore;

    public FirestoreService(@Autowired(required = false) Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean isConnected() {
        return firestore != null;
    }

    public Firestore getFirestore() {
        return firestore;
    }

    /**
     * Save or overwrite a document in a collection
     */
    public void saveDocument(String collectionName, String documentId, Map<String, Object> data) {
        if (!isConnected()) {
            logger.warn("Firestore is not connected. Document {}:{} not saved.", collectionName, documentId);
            return;
        }
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(documentId);
            ApiFuture<WriteResult> result = docRef.set(data);
            logger.info("Saved doc {}:{} at update time: {}", collectionName, documentId, result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.error("Error saving document {}:{}: {}", collectionName, documentId, e.getMessage());
        }
    }

    /**
     * Retrieve a single document
     */
    public Map<String, Object> getDocument(String collectionName, String documentId) {
        if (!isConnected()) return Collections.emptyMap();
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(documentId);
            DocumentSnapshot snapshot = docRef.get().get();
            if (snapshot.exists()) {
                return snapshot.getData();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.error("Error fetching document {}:{}: {}", collectionName, documentId, e.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Retrieve all documents from a collection
     */
    public List<Map<String, Object>> getAllDocuments(String collectionName) {
        if (!isConnected()) return Collections.emptyList();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            List<Map<String, Object>> results = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                Map<String, Object> data = new HashMap<>(doc.getData());
                data.put("id", doc.getId());
                results.add(data);
            }
            return results;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.error("Error fetching collection {}: {}", collectionName, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Delete a document
     */
    public void deleteDocument(String collectionName, String documentId) {
        if (!isConnected()) return;
        try {
            firestore.collection(collectionName).document(documentId).delete().get();
            logger.info("Deleted doc {}:{}", collectionName, documentId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.error("Error deleting document {}:{}: {}", collectionName, documentId, e.getMessage());
        }
    }

    /**
     * Count documents in a collection
     */
    public long countDocuments(String collectionName) {
        if (!isConnected()) return 0;
        try {
            AggregateQuerySnapshot snapshot = firestore.collection(collectionName).count().get().get();
            return snapshot.getCount();
        } catch (Exception e) {
            // Fallback
            return getAllDocuments(collectionName).size();
        }
    }

    /**
     * Store or update user profile in Firestore
     */
    public void recordUser(Long userId, String email, String firstName, String lastName, String role, String phone, String avatarUrl) {
        if (!isConnected()) return;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId);
            data.put("email", email);
            data.put("firstName", firstName);
            data.put("lastName", lastName);
            data.put("role", role);
            data.put("phone", phone);
            data.put("avatarUrl", avatarUrl);
            data.put("updatedAt", new Date());
            String docId = "user_" + (userId != null ? userId : email.replace("@", "_at_").replace(".", "_dot_"));
            saveDocument("users", docId, data);
            logger.info("Recorded user {} in Firestore 'users' collection", email);
        } catch (Exception e) {
            logger.warn("Could not record user in Firestore: {}", e.getMessage());
        }
    }

    /**
     * Record a login event in Firestore (under both 'users' lastLoginAt and 'login_history')
     */
    public void recordLogin(Long userId, String email, String role, String ipAddress, boolean success) {
        if (!isConnected()) return;
        try {
            Date now = new Date();
            // 1. Update user document's lastLoginAt
            String docId = "user_" + (userId != null ? userId : email.replace("@", "_at_").replace(".", "_dot_"));
            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("lastLoginAt", now);
            userUpdate.put("email", email);
            if (role != null) userUpdate.put("role", role);
            saveDocument("users", docId, userUpdate);

            // 2. Add an audit log into login_history collection
            Map<String, Object> history = new HashMap<>();
            history.put("userId", userId);
            history.put("email", email);
            history.put("role", role);
            history.put("ipAddress", ipAddress != null ? ipAddress : "127.0.0.1");
            history.put("status", success ? "SUCCESS" : "FAILED");
            history.put("timestamp", now);
            String logId = "login_" + System.currentTimeMillis();
            saveDocument("login_history", logId, history);
            logger.info("Recorded login event for {} in Firestore 'login_history' collection", email);
        } catch (Exception e) {
            logger.warn("Could not record login event in Firestore: {}", e.getMessage());
        }
    }
}
