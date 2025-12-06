-- Sample Data for Rental Property Information System
-- Run this after schema.sql to populate the database with test data

-- ============================================================================
-- USERS
-- ============================================================================
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('admin', 'admin123', '1'); -- Admin
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('landlord1', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('landlord2', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('landlord3', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('realtor1', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('realtor2', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('property_mgr', 'pass123', '0'); -- Regular User
INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES ('investor1', 'pass123', '0'); -- Regular User

-- ============================================================================
-- PROPERTIES (15 diverse spatial entities)
-- ============================================================================

-- 1. Polygon (Rectangle) - Downtown Studio
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Central Heights', '100 Main St', '101', 1500.00, 1, 45.5, 'Modern studio apartment in the city center with great amenities', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 3),
    SDO_ORDINATE_ARRAY(490, 490, 510, 510)));

-- 2. Polygon - Suburban Family Home
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Green Valley Home', '45 Oak Lane', NULL, 2500.00, 4, 200.0, 'Spacious family home with garden and garage', 3,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(180, 780, 220, 780, 220, 820, 180, 820, 180, 780)));

-- 3. Circle - Riverside Loft
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Riverview Lofts', '12 River Road', '5B', 1800.00, 2, 75.0, 'Industrial style loft with stunning river views', 4,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(800, 280, 820, 300, 800, 320)));

-- 4. Point - Student Housing (keeping as point)
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Student Living', '500 College Ave', '304', 800.00, 1, 30.0, 'Affordable student housing near campus', 5,
    SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(600, 600, NULL), NULL, NULL));

-- 5. Polygon (Hexagon) - Luxury Penthouse
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Sky Tower', '777 High Street', 'PH1', 4500.00, 3, 180.0, 'Exclusive penthouse with panoramic city views', 6,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(430, 450, 450, 430, 470, 430, 490, 450, 470, 470, 450, 470, 430, 450)));

-- 6. Polygon (Complex) - Estate with Grounds
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Grand Estate', '900 Luxury Blvd', NULL, 5000.00, 6, 450.0, 'Massive estate with beautifully landscaped grounds', 7,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(100, 100, 200, 100, 200, 200, 150, 250, 100, 200, 100, 100)));

-- 7. Polygon (Rectangle) - Commercial Building
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Business Plaza', '250 Commerce Dr', NULL, 3500.00, 0, 300.0, 'Prime commercial space in business district', 8,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 3),
    SDO_ORDINATE_ARRAY(700, 700, 800, 800)));

-- 8. Polygon (Square) - Park View Property
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Central Park View', '1 Park Place', '12A', 3200.00, 3, 150.0, 'Beautiful property overlooking the central park', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(400, 100, 450, 100, 450, 150, 400, 150, 400, 100)));

-- 9. Polygon with Hole - Courtyard Property
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('The Courtyard', '8 Roundabout Way', NULL, 2800.00, 2, 120.0, 'Unique architecture with private interior courtyard', 3,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 11, 2003, 4),
    SDO_ORDINATE_ARRAY(600, 100, 800, 100, 800, 300, 600, 300, 600, 100,
                       700, 150, 750, 200, 700, 250)));

-- 10. Multipoint - Storage Facility
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Industrial Hub', '50 Factory Road', NULL, 2000.00, 0, 500.0, 'Multiple storage units available for rent', 4,
    SDO_GEOMETRY(2005, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1, 3),
    SDO_ORDINATE_ARRAY(300, 300, 310, 310, 320, 300)));

-- 11. Circle - Cozy Cottage
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Maple Cottage', '33 Forest Lane', NULL, 1200.00, 2, 65.0, 'Charming cottage in quiet neighborhood', 5,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(250, 640, 260, 650, 250, 660)));

-- 12. Point - Modern Apartment (keeping as point)
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Metro Apartments', '88 Transit Way', '201', 1600.00, 2, 80.0, 'Modern apartment near metro station', 6,
    SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(510, 510, NULL), NULL, NULL));

-- 13. Polygon (L-shaped) - Townhouse
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Riverside Townhomes', '15 Water Street', '3', 2200.00, 3, 140.0, 'Spacious townhouse with river access', 7,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(750, 250, 820, 250, 820, 300, 790, 300, 790, 350, 750, 350, 750, 250)));

-- 14. Polygon (Triangle) - Budget Apartment
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Economy Suites', '200 Budget Ave', '15', 700.00, 1, 35.0, 'Affordable housing option for students and singles', 8,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(540, 640, 560, 640, 550, 660, 540, 640)));

-- 15. Polygon with Multiple Holes - Luxury Villa Complex
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Sunset Villa', '1 Hilltop Drive', NULL, 6000.00, 5, 380.0, 'Exclusive villa with pool and mountain views', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 11, 2003, 4),
    SDO_ORDINATE_ARRAY(50, 50, 150, 50, 150, 150, 50, 150, 50, 50,
                       80, 80, 100, 90, 80, 100)));


-- ============================================================================
-- TRANSIT STOPS (Points)
-- ============================================================================
INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('Central Station', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(505, 505, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('West Bus Terminal', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(150, 150, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('North Metro', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(500, 800, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('East Train Station', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(850, 400, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('South Transit Hub', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(400, 100, NULL), NULL, NULL));

-- ============================================================================
-- SCHOOL CATCHMENT AREAS (Circles/Polygons)
-- ============================================================================

-- Elementary School - Circle approximation
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('ABC Elementary School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(200, 700, 300, 800, 200, 900)));

-- Middle School - Polygon area
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Ville Middle School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(400, 400, 600, 400, 600, 600, 400, 600, 400, 400)));

-- High School - Circle approximation
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Central High School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(700, 600, 850, 750, 700, 900)));

-- International School - Polygon area
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('International Academy',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(100, 400, 300, 400, 300, 600, 100, 600, 100, 400)));

-- ============================================================================
-- CELLULAR COVERAGE (Circles/Polygons)
-- ============================================================================

-- Vodafone - Large coverage area
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('Vodafone',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(0, 0, 500, 500, 0, 1000)));

-- T-Mobile - City center coverage
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('T-Mobile',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(300, 300, 600, 600, 300, 900)));

-- Orange - East side coverage
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('Orange',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(500, 0, 1000, 500, 500, 1000)));

-- 5G FastNet - Full coverage
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('5G FastNet',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(0, 0, 1000, 0, 1000, 1000, 0, 1000, 0, 0)));

-- ============================================================================
-- BIKE ROUTES (Linestrings)
-- ============================================================================

-- Riverfront Path - Scenic route along the river
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Riverfront Path',
    SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1),
    SDO_ORDINATE_ARRAY(750, 200, 800, 250, 850, 300, 850, 400)));

-- Downtown Commuter Lane - Main bike lane through city center
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Downtown Commuter Lane',
    SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1),
    SDO_ORDINATE_ARRAY(400, 400, 450, 450, 500, 500, 550, 550, 600, 600)));

-- University Connector - Links campus to downtown
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('University Connector',
    SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1),
    SDO_ORDINATE_ARRAY(550, 600, 520, 650, 500, 700, 480, 750, 450, 800)));

-- Greenway Trail - Recreational path through green spaces
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Greenway Trail',
    SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1),
    SDO_ORDINATE_ARRAY(100, 700, 150, 750, 200, 800, 250, 850, 300, 900)));

-- East-West Corridor - Major cross-city route
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('East-West Corridor',
    SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1),
    SDO_ORDINATE_ARRAY(100, 500, 200, 500, 300, 500, 400, 500, 500, 500, 600, 500, 700, 500, 800, 500)));

COMMIT;
