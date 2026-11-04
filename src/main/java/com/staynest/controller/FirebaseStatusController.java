package com.staynest.controller;

import com.staynest.service.FirestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseStatusController {

    private final FirestoreService firestoreService;

    public FirebaseStatusController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean connected = firestoreService.isConnected();
        status.put("connected", connected);
        status.put("projectId", "staynest-booking");

        if (connected) {
            Map<String, Object> collections = new HashMap<>();
            collections.put("users", firestoreService.countDocuments("users"));
            collections.put("categories", firestoreService.countDocuments("categories"));
            collections.put("amenities", firestoreService.countDocuments("amenities"));
            collections.put("properties", firestoreService.countDocuments("properties"));
            status.put("collections", collections);
            status.put("message", "Firebase Cloud Firestore is active and connected!");
        } else {
            status.put("message", "Firebase is not connected. Check firebase-service-account.json.");
        }

        return ResponseEntity.ok(status);
    }
}
