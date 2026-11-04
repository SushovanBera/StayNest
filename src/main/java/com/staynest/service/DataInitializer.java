package com.staynest.service;

import com.staynest.entity.*;
import com.staynest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirestoreService firestoreService;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           AmenityRepository amenityRepository,
                           PropertyRepository propertyRepository,
                           PropertyImageRepository propertyImageRepository,
                           BookingRepository bookingRepository,
                           ReviewRepository reviewRepository,
                           PasswordEncoder passwordEncoder,
                           FirestoreService firestoreService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.amenityRepository = amenityRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.firestoreService = firestoreService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0 && propertyRepository.count() > 0) {
            logger.info("StayNest database already contains data. Skipping initial seeding.");
            return;
        }

        logger.info("Initializing StayNest sample data...");

        // 1. Seed Users
        User admin = createUserIfNotFound("admin@staynest.com", "Admin", "StayNest", "admin123",
                Role.ROLE_ADMIN, "+1 555-0100",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                "StayNest Platform Administrator & Quality Curator");

        User host = createUserIfNotFound("host@staynest.com", "Sarah", "Jenkins", "host123",
                Role.ROLE_HOST, "+1 555-0101",
                "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80",
                "Superhost for 6+ years. Architect and passionate traveler hosting curated boutique homes.");

        User guest = createUserIfNotFound("guest@staynest.com", "Alex", "Morgan", "guest123",
                Role.ROLE_GUEST, "+1 555-0102",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=300&q=80",
                "Software designer & remote worker exploring the world one stay at a time.");

        // 2. Seed Categories
        Map<String, Category> categories = new HashMap<>();
        categories.put("beachfront", createCategory("Beachfront", "beachfront", "bi-water", "Steps away from turquoise shores and sandy beaches", 1));
        categories.put("cabins", createCategory("Cabins", "cabins", "bi-tree", "Cozy wooden retreats nestled deep in nature", 2));
        categories.put("villas", createCategory("Villas & Mansions", "villas", "bi-gem", "Architectural marvels with private luxury pools", 3));
        categories.put("countryside", createCategory("Countryside", "countryside", "bi-sun", "Picturesque farmhouses, vineyards, and rolling pastures", 4));
        categories.put("iconic-cities", createCategory("Iconic Cities", "iconic-cities", "bi-buildings", "Chic lofts and designer apartments in world-class capitals", 5));
        categories.put("lakefront", createCategory("Lakefront", "lakefront", "bi-droplet", "Serene lakeside lodges with private docks and canoes", 6));
        categories.put("mountain-retreats", createCategory("Mountain Retreats", "mountain-retreats", "bi-triangle", "Breathtaking alpine chalets with ski-in ski-out access", 7));
        categories.put("luxury", createCategory("Luxury", "luxury", "bi-stars", "Handpicked 5-star estates with premium concierge services", 8));

        // 3. Seed Amenities
        Map<String, Amenity> amenities = new HashMap<>();
        amenities.put("wifi", createAmenity("High-Speed Wi-Fi", "Essentials", "bi-wifi"));
        amenities.put("kitchen", createAmenity("Full Kitchen", "Essentials", "bi-cup-hot"));
        amenities.put("parking", createAmenity("Free Parking", "Features", "bi-car-front"));
        amenities.put("pool", createAmenity("Private Pool", "Features", "bi-water"));
        amenities.put("ac", createAmenity("Air Conditioning", "Essentials", "bi-snow"));
        amenities.put("hottub", createAmenity("Hot Tub / Jacuzzi", "Features", "bi-droplet-fill"));
        amenities.put("bbq", createAmenity("BBQ Grill", "Features", "bi-fire"));
        amenities.put("workspace", createAmenity("Dedicated Workspace", "Essentials", "bi-laptop"));
        amenities.put("tv", createAmenity("Smart TV & Netflix", "Essentials", "bi-tv"));
        amenities.put("pet", createAmenity("Pet Friendly", "General", "bi-heart"));
        amenities.put("washer", createAmenity("Washer & Dryer", "Essentials", "bi-smartwatch"));
        amenities.put("oceanview", createAmenity("Ocean Panorama", "Features", "bi-compass"));
        amenities.put("mountainview", createAmenity("Mountain View", "Features", "bi-geo-alt"));
        amenities.put("gym", createAmenity("Private Gym", "Features", "bi-activity"));
        amenities.put("fireplace", createAmenity("Indoor Fireplace", "Features", "bi-brightness-high"));
        amenities.put("ev", createAmenity("EV Car Charger", "Features", "bi-lightning-charge"));

        // 4. Seed Curated Properties
        // Property 1: Malibu Oceanfront
        Property p1 = new Property();
        p1.setHost(host);
        p1.setCategory(categories.get("beachfront"));
        p1.setTitle("Villa Azure — Oceanfront Infinity Sanctuary");
        p1.setDescription("Perched on the cliffs of Malibu, Villa Azure offers unobstructed 180-degree Pacific ocean panoramas. Features floor-to-ceiling glass walls, a heated infinity pool, private beach path access, and an ultra-modern chef kitchen with Sub-Zero appliances. Perfect for discerning travelers seeking coastal serenity.");
        p1.setPropertyType("Villa");
        p1.setAddress("23805 Pacific Coast Hwy");
        p1.setCity("Malibu");
        p1.setState("California");
        p1.setCountry("United States");
        p1.setZipCode("90265");
        p1.setLatitude(34.0326);
        p1.setLongitude(-118.6896);
        p1.setPricePerNight(new BigDecimal("680.00"));
        p1.setCleaningFee(new BigDecimal("150.00"));
        p1.setServiceFeePercent(new BigDecimal("12.00"));
        p1.setMaxGuests(8);
        p1.setBedrooms(4);
        p1.setBeds(5);
        p1.setBathrooms(4.5);
        p1.setRating(4.96);
        p1.setReviewCount(42);
        p1.setFeatured(true);
        p1.setActive(true);
        p1.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("parking"),
                amenities.get("pool"), amenities.get("ac"), amenities.get("hottub"),
                amenities.get("bbq"), amenities.get("workspace"), amenities.get("tv"),
                amenities.get("oceanview"), amenities.get("ev")
        ));
        p1 = propertyRepository.save(p1);
        addPropertyImage(p1, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80", "Infinity Pool & Ocean Deck", true, 1);
        addPropertyImage(p1, "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=1200&q=80", "Sunlit Living Lounge", false, 2);
        addPropertyImage(p1, "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1200&q=80", "Master Suite with Ocean Panorama", false, 3);
        addPropertyImage(p1, "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80", "Gourmet Chef Kitchen", false, 4);

        // Property 2: Aspen Cabin
        Property p2 = new Property();
        p2.setHost(host);
        p2.setCategory(categories.get("cabins"));
        p2.setTitle("Whispering Pines — Nordic Timber A-Frame Cabin");
        p2.setDescription("A designer timber A-frame retreat surrounded by ancient redwoods and towering pines. Unwind by the crackling wood stove, gaze at starry mountain skies from your cedar hot tub, or explore miles of pine-scented hiking trails right outside your doorstep.");
        p2.setPropertyType("Cabin");
        p2.setAddress("4120 Forest Ridge Road");
        p2.setCity("Aspen");
        p2.setState("Colorado");
        p2.setCountry("United States");
        p2.setZipCode("81611");
        p2.setLatitude(39.1911);
        p2.setLongitude(-106.8175);
        p2.setPricePerNight(new BigDecimal("340.00"));
        p2.setCleaningFee(new BigDecimal("85.00"));
        p2.setServiceFeePercent(new BigDecimal("12.00"));
        p2.setMaxGuests(4);
        p2.setBedrooms(2);
        p2.setBeds(3);
        p2.setBathrooms(2.0);
        p2.setRating(4.91);
        p2.setReviewCount(58);
        p2.setFeatured(true);
        p2.setActive(true);
        p2.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("parking"),
                amenities.get("hottub"), amenities.get("bbq"), amenities.get("workspace"),
                amenities.get("fireplace"), amenities.get("pet"), amenities.get("mountainview")
        ));
        p2 = propertyRepository.save(p2);
        addPropertyImage(p2, "https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=1200&q=80", "A-Frame Forest Lodge", true, 1);
        addPropertyImage(p2, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80", "Rustic Fireplace Lounge", false, 2);
        addPropertyImage(p2, "https://images.unsplash.com/photo-1510798831971-661eb04b3739?auto=format&fit=crop&w=1200&q=80", "Cedar Hot Tub under Redwoods", false, 3);

        // Property 3: Paris Penthouse
        Property p3 = new Property();
        p3.setHost(host);
        p3.setCategory(categories.get("iconic-cities"));
        p3.setTitle("The Parisian Terrace Loft — Saint-Germain");
        p3.setDescription("Sun-drenched top-floor penthouse apartment with historic exposed beams, herringbone parquet, and a private wrought-iron balcony overlooking the Eiffel Tower skyline. Steps from quintessential Parisian bistros, bakeries, and renowned art galleries.");
        p3.setPropertyType("Apartment");
        p3.setAddress("14 Rue de Seine");
        p3.setCity("Paris");
        p3.setState("Île-de-France");
        p3.setCountry("France");
        p3.setZipCode("75006");
        p3.setLatitude(48.8540);
        p3.setLongitude(2.3370);
        p3.setPricePerNight(new BigDecimal("290.00"));
        p3.setCleaningFee(new BigDecimal("75.00"));
        p3.setServiceFeePercent(new BigDecimal("12.00"));
        p3.setMaxGuests(3);
        p3.setBedrooms(1);
        p3.setBeds(2);
        p3.setBathrooms(1.5);
        p3.setRating(4.88);
        p3.setReviewCount(36);
        p3.setFeatured(true);
        p3.setActive(true);
        p3.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("ac"),
                amenities.get("workspace"), amenities.get("tv"), amenities.get("washer")
        ));
        p3 = propertyRepository.save(p3);
        addPropertyImage(p3, "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80", "Parisian Chic Salon", true, 1);
        addPropertyImage(p3, "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80", "Balcony View over Rooftops", false, 2);
        addPropertyImage(p3, "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=1200&q=80", "Artisan Master Bedroom", false, 3);

        // Property 4: Venice Canal Palazzo
        Property p4 = new Property();
        p4.setHost(host);
        p4.setCategory(categories.get("villas"));
        p4.setTitle("Palazzo Bellini — Venetian Grand Canal Villa");
        p4.setDescription("Step back into golden age Venetian elegance in this restored 16th-century palazzo boasting original terrazzo floors, Murano crystal chandeliers, and a private water-taxi gate opening directly onto the Grand Canal. Includes daily breakfast service.");
        p4.setPropertyType("Mansion");
        p4.setAddress("Fondamenta de Cannaregio 48");
        p4.setCity("Venice");
        p4.setState("Veneto");
        p4.setCountry("Italy");
        p4.setZipCode("30121");
        p4.setLatitude(45.4408);
        p4.setLongitude(12.3155);
        p4.setPricePerNight(new BigDecimal("850.00"));
        p4.setCleaningFee(new BigDecimal("200.00"));
        p4.setServiceFeePercent(new BigDecimal("12.00"));
        p4.setMaxGuests(10);
        p4.setBedrooms(5);
        p4.setBeds(6);
        p4.setBathrooms(5.0);
        p4.setRating(4.98);
        p4.setReviewCount(29);
        p4.setFeatured(true);
        p4.setActive(true);
        p4.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("ac"),
                amenities.get("tv"), amenities.get("oceanview"), amenities.get("gym")
        ));
        p4 = propertyRepository.save(p4);
        addPropertyImage(p4, "https://images.unsplash.com/photo-1577495508048-b635879837f1?auto=format&fit=crop&w=1200&q=80", "Grand Venetian Hall", true, 1);
        addPropertyImage(p4, "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80", "Canal Water Terrace", false, 2);

        // Property 5: Zermatt Matterhorn Chalet
        Property p5 = new Property();
        p5.setHost(host);
        p5.setCategory(categories.get("mountain-retreats"));
        p5.setTitle("Matterhorn Alpine Chalet & Private Spa");
        p5.setDescription("Experience the quintessential Swiss ski getaway with direct slope ski-in ski-out access, private pine sauna, outdoor heated jacuzzi, and panoramic unobstructed views of the legendary Matterhorn peak.");
        p5.setPropertyType("Chalet");
        p5.setAddress("Oberdorfstrasse 22");
        p5.setCity("Zermatt");
        p5.setState("Valais");
        p5.setCountry("Switzerland");
        p5.setZipCode("3920");
        p5.setLatitude(45.9765);
        p5.setLongitude(7.7491);
        p5.setPricePerNight(new BigDecimal("560.00"));
        p5.setCleaningFee(new BigDecimal("120.00"));
        p5.setServiceFeePercent(new BigDecimal("12.00"));
        p5.setMaxGuests(6);
        p5.setBedrooms(3);
        p5.setBeds(4);
        p5.setBathrooms(3.0);
        p5.setRating(4.94);
        p5.setReviewCount(64);
        p5.setFeatured(true);
        p5.setActive(true);
        p5.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("parking"),
                amenities.get("hottub"), amenities.get("fireplace"), amenities.get("mountainview"),
                amenities.get("tv")
        ));
        p5 = propertyRepository.save(p5);
        addPropertyImage(p5, "https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=1200&q=80", "Matterhorn Chalet Snow View", true, 1);
        addPropertyImage(p5, "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1200&q=80", "Alpine Fireplace & Lounge", false, 2);

        // Property 6: Tuscan Vineyard Estate
        Property p6 = new Property();
        p6.setHost(host);
        p6.setCategory(categories.get("countryside"));
        p6.setTitle("Tuscan Sun Olive Grove Estate & Vineyard");
        p6.setDescription("A stone villa nestled among centuries-old olive groves and Chianti vineyards. Features a private saltwater pool, pergola dining terrace with wood-fired pizza oven, and wine tasting cellar.");
        p6.setPropertyType("Villa");
        p6.setAddress("Localita San Donato 15");
        p6.setCity("San Gimignano");
        p6.setState("Tuscany");
        p6.setCountry("Italy");
        p6.setZipCode("53037");
        p6.setLatitude(43.4678);
        p6.setLongitude(11.0435);
        p6.setPricePerNight(new BigDecimal("420.00"));
        p6.setCleaningFee(new BigDecimal("95.00"));
        p6.setServiceFeePercent(new BigDecimal("12.00"));
        p6.setMaxGuests(6);
        p6.setBedrooms(3);
        p6.setBeds(4);
        p6.setBathrooms(3.0);
        p6.setRating(4.92);
        p6.setReviewCount(47);
        p6.setFeatured(true);
        p6.setActive(true);
        p6.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("parking"),
                amenities.get("pool"), amenities.get("bbq"), amenities.get("pet"), amenities.get("washer")
        ));
        p6 = propertyRepository.save(p6);
        addPropertyImage(p6, "https://images.unsplash.com/photo-1523217582562-09d0def993a6?auto=format&fit=crop&w=1200&q=80", "Tuscan Countryside Villa", true, 1);
        addPropertyImage(p6, "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80", "Private Olive Garden Pool", false, 2);

        // Property 7: Lake Tahoe Waterfront
        Property p7 = new Property();
        p7.setHost(host);
        p7.setCategory(categories.get("lakefront"));
        p7.setTitle("Emerald Bay Lakehouse & Private Boat Dock");
        p7.setDescription("Direct waterfront luxury on Lake Tahoe with private boat dock, dual kayaks, expansive cedar wraparound deck, outdoor firepit, and majestic alpine water views.");
        p7.setPropertyType("House");
        p7.setAddress("1180 West Lake Blvd");
        p7.setCity("Lake Tahoe");
        p7.setState("California");
        p7.setCountry("United States");
        p7.setZipCode("96145");
        p7.setLatitude(39.1678);
        p7.setLongitude(-120.1450);
        p7.setPricePerNight(new BigDecimal("490.00"));
        p7.setCleaningFee(new BigDecimal("110.00"));
        p7.setServiceFeePercent(new BigDecimal("12.00"));
        p7.setMaxGuests(8);
        p7.setBedrooms(4);
        p7.setBeds(5);
        p7.setBathrooms(3.5);
        p7.setRating(4.89);
        p7.setReviewCount(31);
        p7.setFeatured(false);
        p7.setActive(true);
        p7.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("parking"),
                amenities.get("hottub"), amenities.get("bbq"), amenities.get("pet"), amenities.get("oceanview")
        ));
        p7 = propertyRepository.save(p7);
        addPropertyImage(p7, "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=1200&q=80", "Lakefront Lodge & Dock", true, 1);
        addPropertyImage(p7, "https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?auto=format&fit=crop&w=1200&q=80", "Lakeside Deck & Kayaks", false, 2);

        // Property 8: Kyoto Zen Machiya
        Property p8 = new Property();
        p8.setHost(host);
        p8.setCategory(categories.get("luxury"));
        p8.setTitle("Kyoto Zen Courtyard Machiya & Tea Room");
        p8.setDescription("Traditional Kyoto artisan home thoughtfully modernized with hinoki cedar soaking tub, serene moss rock garden, tatami tea room, and radiant heated floors. Located in the historic Gion district.");
        p8.setPropertyType("House");
        p8.setAddress("Gion Shirakawa 88");
        p8.setCity("Kyoto");
        p8.setState("Kansai");
        p8.setCountry("Japan");
        p8.setZipCode("605-0085");
        p8.setLatitude(35.0037);
        p8.setLongitude(135.7772);
        p8.setPricePerNight(new BigDecimal("380.00"));
        p8.setCleaningFee(new BigDecimal("90.00"));
        p8.setServiceFeePercent(new BigDecimal("12.00"));
        p8.setMaxGuests(4);
        p8.setBedrooms(2);
        p8.setBeds(3);
        p8.setBathrooms(2.0);
        p8.setRating(4.95);
        p8.setReviewCount(52);
        p8.setFeatured(true);
        p8.setActive(true);
        p8.getAmenities().addAll(Arrays.asList(
                amenities.get("wifi"), amenities.get("kitchen"), amenities.get("ac"),
                amenities.get("hottub"), amenities.get("workspace"), amenities.get("washer"), amenities.get("fireplace")
        ));
        p8 = propertyRepository.save(p8);
        addPropertyImage(p8, "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1200&q=80", "Kyoto Traditional Facade", true, 1);
        addPropertyImage(p8, "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1200&q=80", "Private Zen Courtyard Garden", false, 2);

        // 5. Seed Initial Sample Bookings & Reviews for guest
        Booking b1 = new Booking();
        b1.setBookingCode("SN-2026-9812");
        b1.setProperty(p1);
        b1.setUser(guest);
        b1.setCheckIn(LocalDate.now().plusDays(15));
        b1.setCheckOut(LocalDate.now().plusDays(19));
        b1.setGuestsCount(4);
        b1.setNightsCount(4);
        b1.setBasePrice(new BigDecimal("2720.00"));
        b1.setCleaningFee(new BigDecimal("150.00"));
        b1.setServiceFee(new BigDecimal("326.40"));
        b1.setTaxesAmount(new BigDecimal("255.71"));
        b1.setTotalPrice(new BigDecimal("3452.11"));
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setPaymentStatus("PAID");
        b1.setPaymentMethod("DEMO_CARD");
        b1.setSpecialRequests("Late check-in requested around 8 PM. Celebrating our anniversary.");
        b1 = bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBookingCode("SN-2026-4421");
        b2.setProperty(p2);
        b2.setUser(guest);
        b2.setCheckIn(LocalDate.now().minusDays(10));
        b2.setCheckOut(LocalDate.now().minusDays(6));
        b2.setGuestsCount(2);
        b2.setNightsCount(4);
        b2.setBasePrice(new BigDecimal("1360.00"));
        b2.setCleaningFee(new BigDecimal("85.00"));
        b2.setServiceFee(new BigDecimal("163.20"));
        b2.setTaxesAmount(new BigDecimal("128.66"));
        b2.setTotalPrice(new BigDecimal("1736.86"));
        b2.setStatus(BookingStatus.COMPLETED);
        b2.setPaymentStatus("PAID");
        b2.setPaymentMethod("DEMO_CARD");
        b2 = bookingRepository.save(b2);

        // Seed Reviews
        Review r1 = new Review(p1, guest, null, 5,
                "Absolute paradise! The sunset from the infinity pool was life-changing. Sarah was a phenomenal host who ensured every detail was impeccable. Can't wait to return!");
        reviewRepository.save(r1);

        Review r2 = new Review(p2, guest, b2, 5,
                "The coziest cabin we have ever stayed in. The wood stove was stocked with firewood, and the outdoor hot tub under the starry redwood canopy was magical.");
        reviewRepository.save(r2);

        logger.info("StayNest sample data initialized successfully! Ready to accept reservations.");
    }

    private User createUserIfNotFound(String email, String firstName, String lastName, String rawPassword,
                                      Role role, String phone, String avatar, String bio) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setRole(role);
            u.setPhone(phone);
            u.setAvatarUrl(avatar);
            u.setBio(bio);
            u.setActive(true);
            User saved = userRepository.save(u);
            firestoreService.recordUser(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(),
                    saved.getRole().name(), saved.getPhone(), saved.getAvatarUrl());
            return saved;
        });
    }

    private Category createCategory(String name, String slug, String icon, String description, int order) {
        return categoryRepository.findBySlug(slug).orElseGet(() ->
                categoryRepository.save(new Category(name, slug, icon, description, order))
        );
    }

    private Amenity createAmenity(String name, String category, String iconClass) {
        return amenityRepository.findByName(name).orElseGet(() ->
                amenityRepository.save(new Amenity(name, category, iconClass))
        );
    }

    private void addPropertyImage(Property property, String url, String caption, boolean primary, int order) {
        PropertyImage img = new PropertyImage(property, url, caption, primary, order);
        propertyImageRepository.save(img);
    }
}
