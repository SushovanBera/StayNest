package com.staynest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(2)
public class FirestoreDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreDataInitializer.class);

    private final FirestoreService firestoreService;
    private final PasswordEncoder passwordEncoder;

    public FirestoreDataInitializer(FirestoreService firestoreService, PasswordEncoder passwordEncoder) {
        this.firestoreService = firestoreService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!firestoreService.isConnected()) {
            logger.info("Firestore is not connected. Skipping Firestore sample data initialization.");
            return;
        }

        long userCount = firestoreService.countDocuments("users");
        if (userCount > 0) {
            logger.info("Firestore already contains data ({} users found). Skipping initialization.", userCount);
            return;
        }

        logger.info("⚡ Seeding Cloud Firestore with StayNest sample data...");

        // 1. Seed Users
        seedUsers();

        // 2. Seed Categories
        seedCategories();

        // 3. Seed Amenities
        seedAmenities();

        // 4. Seed Properties
        seedProperties();

        logger.info("✅ Firestore sample data initialization completed successfully!");
    }

    private void seedUsers() {
        Map<String, Object> admin = new HashMap<>();
        admin.put("email", "admin@staynest.com");
        admin.put("firstName", "Admin");
        admin.put("lastName", "StayNest");
        admin.put("password", passwordEncoder.encode("admin123"));
        admin.put("role", "ROLE_ADMIN");
        admin.put("phone", "+1 555-0100");
        admin.put("avatarUrl", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80");
        admin.put("bio", "StayNest Platform Administrator & Quality Curator");
        admin.put("isActive", true);
        admin.put("createdAt", new Date());
        firestoreService.saveDocument("users", "user_admin", admin);

        Map<String, Object> host = new HashMap<>();
        host.put("email", "host@staynest.com");
        host.put("firstName", "Sarah");
        host.put("lastName", "Jenkins");
        host.put("password", passwordEncoder.encode("host123"));
        host.put("role", "ROLE_HOST");
        host.put("phone", "+1 555-0101");
        host.put("avatarUrl", "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80");
        host.put("bio", "Superhost for 6+ years. Architect and passionate traveler hosting curated boutique homes.");
        host.put("isActive", true);
        host.put("createdAt", new Date());
        firestoreService.saveDocument("users", "user_host", host);

        Map<String, Object> guest = new HashMap<>();
        guest.put("email", "guest@staynest.com");
        guest.put("firstName", "Alex");
        guest.put("lastName", "Morgan");
        guest.put("password", passwordEncoder.encode("guest123"));
        guest.put("role", "ROLE_GUEST");
        guest.put("phone", "+1 555-0102");
        guest.put("avatarUrl", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=300&q=80");
        guest.put("bio", "Software designer & remote worker exploring the world one stay at a time.");
        guest.put("isActive", true);
        guest.put("createdAt", new Date());
        firestoreService.saveDocument("users", "user_guest", guest);
    }

    private void seedCategories() {
        String[][] cats = {
            {"beachfront", "Beachfront", "bi-water", "Steps away from turquoise shores and sandy beaches", "1"},
            {"cabins", "Cabins", "bi-tree", "Cozy wooden retreats nestled deep in nature", "2"},
            {"villas", "Villas & Mansions", "bi-gem", "Architectural marvels with private luxury pools", "3"},
            {"countryside", "Countryside", "bi-sun", "Picturesque farmhouses, vineyards, and rolling pastures", "4"},
            {"iconic-cities", "Iconic Cities", "bi-buildings", "Chic lofts and designer apartments in world capitals", "5"},
            {"lakefront", "Lakefront", "bi-droplet", "Serene lakeside lodges with private docks and canoes", "6"},
            {"mountain-retreats", "Mountain Retreats", "bi-triangle", "Breathtaking alpine chalets with ski-in access", "7"},
            {"luxury", "Luxury", "bi-stars", "Handpicked 5-star estates with premium concierge services", "8"}
        };

        for (String[] c : cats) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("slug", c[0]);
            doc.put("name", c[1]);
            doc.put("icon", c[2]);
            doc.put("description", c[3]);
            doc.put("displayOrder", Integer.parseInt(c[4]));
            firestoreService.saveDocument("categories", c[0], doc);
        }
    }

    private void seedAmenities() {
        String[][] ams = {
            {"wifi", "High-Speed Wi-Fi", "Essentials", "bi-wifi"},
            {"kitchen", "Full Kitchen", "Essentials", "bi-cup-hot"},
            {"parking", "Free Parking", "Features", "bi-car-front"},
            {"pool", "Private Pool", "Features", "bi-water"},
            {"ac", "Air Conditioning", "Essentials", "bi-snow"},
            {"hottub", "Hot Tub / Jacuzzi", "Features", "bi-droplet-fill"},
            {"bbq", "BBQ Grill", "Features", "bi-fire"},
            {"workspace", "Dedicated Workspace", "Essentials", "bi-laptop"},
            {"tv", "Smart TV & Streaming", "Essentials", "bi-tv"},
            {"pet", "Pet Friendly", "General", "bi-heart"},
            {"washer", "Washer & Dryer", "Essentials", "bi-smartwatch"},
            {"oceanview", "Ocean Panorama", "Features", "bi-compass"},
            {"mountainview", "Mountain View", "Features", "bi-geo-alt"},
            {"gym", "Fitness Gym", "Features", "bi-activity"},
            {"fireplace", "Indoor Fireplace", "Features", "bi-brightness-high"},
            {"ev", "EV Car Charger", "Features", "bi-lightning-charge"}
        };

        for (String[] a : ams) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", a[0]);
            doc.put("name", a[1]);
            doc.put("category", a[2]);
            doc.put("iconClass", a[3]);
            firestoreService.saveDocument("amenities", a[0], doc);
        }
    }

    private void seedProperties() {
        // Property 1: Villa Azure
        Map<String, Object> p1 = new HashMap<>();
        p1.put("title", "Villa Azure — Oceanfront Infinity Sanctuary");
        p1.put("description", "Perched on the cliffs of Malibu, Villa Azure offers unobstructed 180-degree Pacific ocean panoramas. Features floor-to-ceiling glass walls, a heated infinity pool, private beach path access, and an ultra-modern chef kitchen.");
        p1.put("propertyType", "Villa");
        p1.put("categorySlug", "beachfront");
        p1.put("hostEmail", "host@staynest.com");
        p1.put("address", "23805 Pacific Coast Hwy");
        p1.put("city", "Malibu");
        p1.put("state", "California");
        p1.put("country", "United States");
        p1.put("pricePerNight", 680.00);
        p1.put("cleaningFee", 150.00);
        p1.put("maxGuests", 8);
        p1.put("bedrooms", 4);
        p1.put("bathrooms", 4.5);
        p1.put("rating", 4.96);
        p1.put("reviewCount", 42);
        p1.put("isFeatured", true);
        p1.put("isActive", true);
        p1.put("images", Arrays.asList(
            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1200&q=80"
        ));
        p1.put("amenities", Arrays.asList("High-Speed Wi-Fi", "Full Kitchen", "Private Pool", "Air Conditioning", "Hot Tub / Jacuzzi", "Ocean Panorama"));
        firestoreService.saveDocument("properties", "prop_malibu_azure", p1);

        // Property 2: Aspen Timber Cabin
        Map<String, Object> p2 = new HashMap<>();
        p2.put("title", "Whispering Pines — Nordic Timber A-Frame Cabin");
        p2.put("description", "A designer timber A-frame retreat surrounded by ancient redwoods and towering pines. Unwind by the crackling wood stove or gaze at starry mountain skies from your cedar hot tub.");
        p2.put("propertyType", "Cabin");
        p2.put("categorySlug", "cabins");
        p2.put("hostEmail", "host@staynest.com");
        p2.put("address", "4120 Forest Ridge Road");
        p2.put("city", "Aspen");
        p2.put("state", "Colorado");
        p2.put("country", "United States");
        p2.put("pricePerNight", 420.00);
        p2.put("cleaningFee", 100.00);
        p2.put("maxGuests", 6);
        p2.put("bedrooms", 3);
        p2.put("bathrooms", 2.0);
        p2.put("rating", 4.98);
        p2.put("reviewCount", 38);
        p2.put("isFeatured", true);
        p2.put("isActive", true);
        p2.put("images", Arrays.asList(
            "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=1200&q=80"
        ));
        p2.put("amenities", Arrays.asList("High-Speed Wi-Fi", "Full Kitchen", "Hot Tub / Jacuzzi", "Indoor Fireplace", "Mountain View"));
        firestoreService.saveDocument("properties", "prop_aspen_pines", p2);
    }
}
