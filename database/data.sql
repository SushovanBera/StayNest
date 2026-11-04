-- ===================================================================
-- StayNest Sample Data (MySQL 8.0+)
-- ===================================================================
USE staynest_db;

-- 1. Insert Users (Passwords: admin123, host123, guest123 - BCrypt hashed)
-- BCrypt for 'admin123' / 'host123' / 'guest123' generated via standard BCrypt(10)
INSERT INTO users (id, first_name, last_name, email, password, phone, role, avatar_url, bio, is_active) VALUES
(1, 'Admin', 'StayNest', 'admin@staynest.com', '$2a$10$e8wFp1Ew5Rz6.O7fXzR4feS4dM61JpU4e/xJ1pUGPz4o4nElnBv6i', '+1 555-0100', 'ROLE_ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80', 'StayNest Platform Administrator', TRUE),
(2, 'Sarah', 'Jenkins', 'host@staynest.com', '$2a$10$qIeql4XN9aA10yFzS6zSReV5V6nB1Q4v9W8mP0eK2eL9kL2kL2kL2', '+1 555-0101', 'ROLE_HOST', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80', 'Superhost with 6 years of experience offering boutique vacation homes.', TRUE),
(3, 'Alex', 'Morgan', 'guest@staynest.com', '$2a$10$8sK2P5mN9aA10yFzS6zSReV5V6nB1Q4v9W8mP0eK2eL9kL2kL2kL2', '+1 555-0102', 'ROLE_GUEST', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=300&q=80', 'Passionate globe-trotter and remote software architect.', TRUE)
ON DUPLICATE KEY UPDATE email=email;

-- 2. Insert Categories
INSERT INTO categories (id, name, slug, icon, description, display_order) VALUES
(1, 'Beachfront', 'beachfront', 'bi-water', 'Steps away from turquoise shores and sandy beaches', 1),
(2, 'Cabins', 'cabins', 'bi-tree', 'Cozy wooden retreats nestled deep in nature', 2),
(3, 'Villas & Mansions', 'villas', 'bi-gem', 'Architectural marvels with private luxury pools', 3),
(4, 'Countryside', 'countryside', 'bi-sun', 'Picturesque farmhouses, vineyards, and rolling pastures', 4),
(5, 'Iconic Cities', 'iconic-cities', 'bi-buildings', 'Chic lofts and designer apartments in world-class capitals', 5),
(6, 'Lakefront', 'lakefront', 'bi-droplet', 'Serene lakeside lodges with private docks and canoes', 6),
(7, 'Mountain Retreats', 'mountain-retreats', 'bi-triangle', 'Breathtaking alpine chalets with ski-in ski-out access', 7),
(8, 'Luxury', 'luxury', 'bi-stars', 'Handpicked 5-star estates with premium concierge services', 8)
ON DUPLICATE KEY UPDATE slug=slug;

-- 3. Insert Amenities
INSERT INTO amenities (id, name, category, icon_class) VALUES
(1, 'High-Speed Wi-Fi', 'Essentials', 'bi-wifi'),
(2, 'Full Kitchen', 'Essentials', 'bi-cup-hot'),
(3, 'Free Parking', 'Features', 'bi-car-front'),
(4, 'Private Pool', 'Features', 'bi-water'),
(5, 'Air Conditioning', 'Essentials', 'bi-snow'),
(6, 'Hot Tub / Jacuzzi', 'Features', 'bi-droplet-fill'),
(7, 'BBQ Grill', 'Features', 'bi-fire'),
(8, 'Dedicated Workspace', 'Essentials', 'bi-laptop'),
(9, 'Smart TV & Streaming', 'Essentials', 'bi-tv'),
(10, 'Pet Friendly', 'General', 'bi-heart'),
(11, 'Washer & Dryer', 'Essentials', 'bi-smartwatch'),
(12, 'Ocean Panorama', 'Features', 'bi-compass'),
(13, 'Mountain View', 'Features', 'bi-geo-alt'),
(14, 'Fitness Gym', 'Features', 'bi-activity'),
(15, 'Indoor Fireplace', 'Features', 'bi-brightness-high'),
(16, 'EV Car Charger', 'Features', 'bi-lightning-charge')
ON DUPLICATE KEY UPDATE name=name;

-- 4. Insert Properties
INSERT INTO properties (id, host_id, category_id, title, description, property_type, address, city, state, country, zip_code, latitude, longitude, price_per_night, cleaning_fee, service_fee_percent, max_guests, bedrooms, beds, bathrooms, rating, review_count, is_featured, is_active) VALUES
(1, 2, 1, 'Villa Azure — Oceanfront Infinity Sanctuary', 
'Perched on the cliffs of Malibu, Villa Azure offers unobstructed 180-degree Pacific ocean panoramas. Features floor-to-ceiling glass walls, a heated infinity pool, private beach path access, and an ultra-modern chef kitchen with Sub-Zero appliances.',
'Villa', '23805 Pacific Coast Hwy', 'Malibu', 'California', 'United States', '90265', 34.0326, -118.6896, 680.00, 150.00, 12.00, 8, 4, 5, 4.5, 4.96, 42, TRUE, TRUE),

(2, 2, 2, 'Whispering Pines — Nordic A-Frame Cabin', 
'A designer timber A-frame retreat surrounded by ancient redwoods. Unwind by the crackling wood stove, gaze at starry mountain skies from your cedar hot tub, or explore miles of pine-scented hiking trails right outside your doorstep.',
'Cabin', '4120 Forest Ridge Road', 'Aspen', 'Colorado', 'United States', '81611', 39.1911, -106.8175, 340.00, 85.00, 12.00, 4, 2, 3, 2.0, 4.91, 58, TRUE, TRUE),

(3, 2, 5, 'The Parisian Terrace Loft — Saint-Germain', 
'Sun-drenched top-floor penthouse apartment with historic exposed beams, herringbone parquet, and a private wrought-iron balcony overlooking the Eiffel Tower skyline. Steps from Parisian bistros, boulangeries, and art galleries.',
'Apartment', '14 Rue de Seine', 'Paris', 'Île-de-France', 'France', '75006', 48.8540, 2.3370, 290.00, 75.00, 12.00, 3, 1, 2, 1.5, 4.88, 36, TRUE, TRUE),

(4, 2, 3, 'Palazzo Bellini — Venetian Canal Mansion', 
'Step back in elegance inside this restored 16th-century palazzo boasting original terrazzo floors, Murano crystal chandeliers, and a private water-taxi gate opening directly onto the Grand Canal.',
'Mansion', 'Fondamenta de Cannaregio 48', 'Venice', 'Veneto', 'Italy', '30121', 45.4408, 12.3155, 850.00, 200.00, 12.00, 10, 5, 6, 5.0, 4.98, 29, TRUE, TRUE),

(5, 2, 7, 'Matterhorn Alpine Chalet & Spa', 
'Experience the quintessential Swiss ski getaway with direct slope access, private pine sauna, outdoor heated jacuzzi, and panoramic views of the legendary Matterhorn peak.',
'Chalet', 'Oberdorfstrasse 22', 'Zermatt', 'Valais', 'Switzerland', '3920', 45.9765, 7.7491, 560.00, 120.00, 12.00, 6, 3, 4, 3.0, 4.94, 64, TRUE, TRUE),

(6, 2, 4, 'Tuscan Sun Olive Grove Estate', 
'A stone villa nestled among centuries-old olive groves and Chianti vineyards. Features a private saltwater pool, pergola dining terrace with wood-fired pizza oven, and wine tasting cellar.',
'Villa', 'Localita San Donato 15', 'San Gimignano', 'Tuscany', 'Italy', '53037', 43.4678, 11.0435, 420.00, 95.00, 12.00, 6, 3, 4, 3.0, 4.92, 47, TRUE, TRUE),

(7, 2, 6, 'Emerald Bay Lakehouse & Private Dock', 
'Direct waterfront luxury on Lake Tahoe with private boat dock, dual kayaks, expansive cedar wraparound deck, outdoor firepit, and majestic alpine water views.',
'House', '1180 West Lake Blvd', 'Lake Tahoe', 'California', 'United States', '96145', 39.1678, -120.1450, 490.00, 110.00, 12.00, 8, 4, 5, 3.5, 4.89, 31, FALSE, TRUE),

(8, 2, 8, 'Kyoto Zen Courtyard Machiya', 
'Traditional Kyoto artisan home thoughtfully modernized with hinoki cedar soaking tub, serene moss rock garden, tatami tea room, and radiant heated floors.',
'House', 'Gion Shirakawa 88', 'Kyoto', 'Kansai', 'Japan', '605-0085', 35.0037, 135.7772, 380.00, 90.00, 12.00, 4, 2, 3, 2.0, 4.95, 52, TRUE, TRUE)
ON DUPLICATE KEY UPDATE title=title;

-- 5. Insert Property Images
INSERT INTO property_images (property_id, image_url, caption, is_primary, display_order) VALUES
(1, 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80', 'Front Infinity Pool & Ocean Deck', TRUE, 1),
(1, 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=1200&q=80', 'Contemporary Living Room with Sea View', FALSE, 2),
(1, 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1200&q=80', 'Master Suite Overlooking Malibu Shore', FALSE, 3),
(1, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80', 'Gourmet Kitchen & Dining', FALSE, 4),

(2, 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=1200&q=80', 'A-Frame Cabin in the Redwoods', TRUE, 1),
(2, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80', 'Warm Hearth & Loft Bedroom', FALSE, 2),
(2, 'https://images.unsplash.com/photo-1510798831971-661eb04b3739?auto=format&fit=crop&w=1200&q=80', 'Outdoor Cedar Hot Tub', FALSE, 3),

(3, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80', 'Chic Parisian Living Room', TRUE, 1),
(3, 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80', 'Balcony with Paris Skyline', FALSE, 2),
(3, 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=1200&q=80', 'Bright Bedroom Suite', FALSE, 3),

(4, 'https://images.unsplash.com/photo-1577495508048-b635879837f1?auto=format&fit=crop&w=1200&q=80', 'Venetian Palazzo Grand Hall', TRUE, 1),
(4, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80', 'Canal Terrace Dining', FALSE, 2),

(5, 'https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=1200&q=80', 'Zermatt Chalet Snow View', TRUE, 1),
(5, 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1200&q=80', 'Alpine Spa & Fireplace', FALSE, 2),

(6, 'https://images.unsplash.com/photo-1523217582562-09d0def993a6?auto=format&fit=crop&w=1200&q=80', 'Tuscan Villa & Olive Grove', TRUE, 1),
(6, 'https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80', 'Private Tuscan Pool', FALSE, 2),

(7, 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=1200&q=80', 'Lakefront Lodge & Dock', TRUE, 1),
(7, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?auto=format&fit=crop&w=1200&q=80', 'Lakeside Patio & Kayaks', FALSE, 2),

(8, 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1200&q=80', 'Traditional Japanese Machiya', TRUE, 1),
(8, 'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1200&q=80', 'Zen Garden & Hinoki Bath', FALSE, 2);

-- 6. Link Property Amenities
INSERT INTO property_amenities (property_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 12),
(2, 1), (2, 2), (2, 3), (2, 6), (2, 7), (2, 8), (2, 10), (2, 13), (2, 15),
(3, 1), (3, 2), (3, 5), (3, 8), (3, 9), (3, 11),
(4, 1), (4, 2), (4, 5), (4, 9), (4, 12), (4, 14),
(5, 1), (5, 2), (5, 3), (5, 6), (5, 8), (5, 9), (5, 13), (5, 15),
(6, 1), (6, 2), (6, 3), (6, 4), (6, 7), (6, 10), (6, 11),
(7, 1), (7, 2), (7, 3), (7, 6), (7, 7), (7, 10), (7, 12),
(8, 1), (8, 2), (8, 5), (8, 6), (8, 8), (8, 11), (8, 15);

-- 7. Insert Sample Bookings
INSERT INTO bookings (id, booking_code, property_id, user_id, check_in, check_out, guests_count, nights_count, base_price, cleaning_fee, service_fee, taxes_amount, total_price, status, payment_status, payment_method, special_requests) VALUES
(1, 'SN-2026-9812', 1, 3, '2026-11-10', '2026-11-14', 4, 4, 2720.00, 150.00, 326.40, 217.60, 3414.00, 'CONFIRMED', 'PAID', 'DEMO_CARD', 'Late check-in requested around 8 PM.'),
(2, 'SN-2026-4421', 2, 3, '2026-12-20', '2026-12-24', 2, 4, 1360.00, 85.00, 163.20, 108.80, 1717.00, 'CONFIRMED', 'PAID', 'DEMO_CARD', 'Traveling for winter holiday celebration.')
ON DUPLICATE KEY UPDATE booking_code=booking_code;

-- 8. Insert Sample Reviews
INSERT INTO reviews (property_id, user_id, booking_id, rating, comment, is_moderated) VALUES
(1, 3, 1, 5, 'Absolute paradise! The sunset from the infinity pool was life-changing. Sarah was a phenomenal host who ensured every detail was impeccable.', FALSE),
(2, 3, 2, 5, 'The coziest cabin we have ever stayed in. The wood stove was stocked with firewood, and the outdoor hot tub under the snowy pine trees was magical.', FALSE);
