-- Sample Data for Rental Property Information System
-- Run this after schema.sql to populate the database with test data
-- Coordinate system: 0-1000 for both X and Y axes

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
-- PROPERTIES
-- ============================================================================

-- 1. Polygon (Rectangle) - Namesti Svobody Area
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Namesti Svobody Residence', 'Namesti Svobody 15', '3A', 2800.00, 2, 85.0, 'Modern apartment in the heart of Brno with stunning views of the historic square', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 3),
    SDO_ORDINATE_ARRAY(480, 480, 520, 520)));

-- 2. Polygon (Complex) - Spilberk Castle View
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Spilberk View Apartments', 'Pellicova 12', '5B', 3200.00, 3, 120.0, 'Luxury apartment with panoramic views of Spilberk Castle and the city', 3,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(350, 650, 420, 650, 420, 720, 380, 740, 350, 720, 350, 650)));

-- 3. Circle - Luzanky Park Residence
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Luzanky Park Tower', 'Sportovni 8', '12C', 2500.00, 2, 75.0, 'Circular design building overlooking the beautiful Luzanky Park', 4,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(250, 350, 280, 380, 250, 410)));

-- 4. Polygon (Hexagon) - Cabbage Market Loft
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Cabbage Market Lofts', 'Cabbage Market 5', 'PH1', 4200.00, 3, 150.0, 'Exclusive penthouse near the historic Cabbage Market with modern amenities', 5,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(520, 450, 550, 440, 580, 450, 580, 480, 550, 490, 520, 480, 520, 450)));

-- 5. Polygon with Hole - Denis Gardens Courtyard
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Denis Gardens Complex', 'Denis Gardens 22', NULL, 3500.00, 4, 180.0, 'Unique property with private interior courtyard garden in prestigious Denis Gardens area', 6,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 11, 2003, 4),
    SDO_ORDINATE_ARRAY(650, 250, 750, 250, 750, 350, 650, 350, 650, 250,
                       690, 280, 720, 300, 690, 320)));

-- 6. Multipolygon - Veveri Street Complex
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Veveri Street Estates', 'Veveri 45', NULL, 5500.00, 5, 250.0, 'Multi-building estate complex on prestigious Veveri Street', 7,
    SDO_GEOMETRY(2007, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 11, 1003, 1),
    SDO_ORDINATE_ARRAY(100, 600, 150, 600, 150, 650, 100, 650, 100, 600,
                       160, 600, 200, 600, 200, 650, 160, 650, 160, 600)));

-- 7. Polygon (Rectangle) - Masaryk University District
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('University Heights', 'Kotlarska 18', '204', 1800.00, 1, 45.0, 'Student-friendly apartment near Masaryk University campus', 8,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 3),
    SDO_ORDINATE_ARRAY(580, 620, 620, 660)));

-- 8. Circle - Brno Reservoir View
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Reservoir View Residence', 'Pristavni 32', '8A', 3800.00, 3, 135.0, 'Premium property with views of Brno Reservoir and surrounding nature', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(800, 750, 840, 790, 800, 830)));

-- 9. Polygon (L-shaped) - Petrov Cathedral Quarter
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Petrov Cathedral Apartments', 'Petrov Hill 7', '3', 3600.00, 3, 140.0, 'Historic building in the shadow of the iconic Petrov Cathedral', 3,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(440, 520, 480, 520, 480, 560, 460, 560, 460, 580, 440, 580, 440, 520)));

-- 10. Polygon (Triangle) - Mendel Square Budget
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Mendel Square Studios', 'Mendel Square 9', '15', 1200.00, 1, 35.0, 'Affordable studio apartment near Mendel Square, perfect for students', 4,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(320, 480, 360, 480, 340, 520, 320, 480)));

-- 11. Polygon with Multiple Holes - Tugendhat Villa District
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Tugendhat District Villa', 'Cernopolni 45', NULL, 8500.00, 6, 420.0, 'Exclusive modernist villa in the prestigious Tugendhat Villa neighborhood', 5,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 13, 2003, 4, 19, 2003, 4),
    SDO_ORDINATE_ARRAY(750, 450, 850, 450, 850, 550, 750, 550, 750, 450,
                       780, 470, 800, 485, 780, 500,
                       820, 470, 840, 485, 820, 500)));

-- 12. Polygon (Pentagon) - Janacek Theatre District
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Janacek Cultural Center', 'Rooseveltova 7', '6B', 2900.00, 2, 90.0, 'Modern apartment near Janacek Theatre and cultural venues', 6,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(200, 200, 240, 180, 280, 200, 260, 240, 220, 240, 200, 200)));

-- 13. Circle - Brno Exhibition Center Area
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Exhibition Center Suites', 'Vystaviste 1', '10D', 2200.00, 2, 70.0, 'Convenient location near Brno Exhibition Center, ideal for business travelers', 7,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(150, 800, 175, 825, 150, 850)));

-- 14. Polygon (Octagon) - Moravian Square Premium
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Moravian Square Tower', 'Moravian Square 3', 'PH2', 5200.00, 4, 200.0, 'Luxury octagonal penthouse overlooking Moravian Square shopping district', 8,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(500, 300, 530, 290, 560, 300, 570, 330, 560, 360, 530, 370, 500, 360, 490, 330, 500, 300)));

-- 15. Polygon (Rectangle) - Svratka River Apartments
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Svratka Riverside', 'Riverfront Boulevard 28', '7C', 2600.00, 2, 80.0, 'Peaceful riverside apartment along the Svratka River with bike path access', 2,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 3),
    SDO_ORDINATE_ARRAY(680, 580, 720, 620)));

-- 16. Multipolygon - Old Town Complex
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Old Town Heritage Buildings', 'Old Brno Street 12', NULL, 4800.00, 4, 190.0, 'Historic multi-building complex in the charming Old Town district', 3,
    SDO_GEOMETRY(2007, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1, 11, 1003, 1),
    SDO_ORDINATE_ARRAY(380, 380, 420, 380, 420, 420, 380, 420, 380, 380,
                       430, 380, 460, 380, 460, 420, 430, 420, 430, 380)));

-- 17. Polygon (Square) - Brno Technology Park
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Tech Park Innovation Hub', 'Technology Park 50', '301', 2100.00, 1, 55.0, 'Modern apartment in Brno Technology Park, perfect for IT professionals', 4,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 1),
    SDO_ORDINATE_ARRAY(850, 200, 890, 200, 890, 240, 850, 240, 850, 200)));

-- 18. Circle - Kraavi Hill Residence
INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY)
VALUES ('Kraavi Hill Estates', 'Kraavi 16', NULL, 4500.00, 4, 165.0, 'Spacious family home on scenic Kraavi Hill with panoramic city views', 5,
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(300, 150, 335, 185, 300, 220)));


-- ============================================================================
-- SCHOOL CATCHMENT AREAS (Moderately sized circles)
-- ============================================================================

-- Elementary School 1 - Circle (moderate size ~100 unit radius)
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Central Elementary School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(300, 400, 400, 500, 300, 600)));

-- Elementary School 2 - Circle (moderate size)
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Northside Elementary School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(600, 700, 700, 800, 600, 900)));

-- Middle School - Circle (moderate size)
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Riverside Middle School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(700, 300, 800, 400, 700, 500)));

-- High School - Circle (moderate size)
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Brno International High School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(150, 650, 250, 750, 150, 850)));

-- Specialized School - Circle (moderate size)
INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY)
VALUES ('Technical Vocational School',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(800, 100, 900, 200, 800, 300)));


-- ============================================================================
-- TRANSIT STOPS (Mix of Points and Multipoints)
-- ============================================================================

-- Single Point Stops
INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('Main Railway Station', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(500, 500, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('Freedom Square Tram Stop', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(485, 495, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('Spilberk Castle Bus Stop', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(380, 690, NULL), NULL, NULL));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) 
VALUES ('Exhibition Center Metro', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(165, 825, NULL), NULL, NULL));

-- Multipoint Stops (representing multiple platforms or entrances)
INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY)
VALUES ('Grand Central Transit Hub',
    SDO_GEOMETRY(2005, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1, 4),
    SDO_ORDINATE_ARRAY(480, 480, 490, 485, 500, 490, 510, 485)));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY)
VALUES ('University Campus Stops',
    SDO_GEOMETRY(2005, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1, 3),
    SDO_ORDINATE_ARRAY(590, 630, 600, 640, 610, 650)));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY)
VALUES ('Technology Park Transit Center',
    SDO_GEOMETRY(2005, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1, 5),
    SDO_ORDINATE_ARRAY(860, 210, 870, 215, 880, 220, 870, 225, 860, 230)));

INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY)
VALUES ('Riverside Multi-Platform',
    SDO_GEOMETRY(2005, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1, 3),
    SDO_ORDINATE_ARRAY(690, 590, 700, 600, 710, 610)));


-- ============================================================================
-- CELLULAR COVERAGE (Moderately sized circles - Czech providers)
-- ============================================================================

-- T-Mobile CZ - Circle (moderate coverage)
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('T-Mobile Czech Republic',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(300, 300, 500, 500, 300, 700)));

-- O2 Czech Republic - Circle (moderate coverage)
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('O2 Czech Republic',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(500, 300, 700, 500, 500, 700)));

-- Vodafone CZ - Circle (moderate coverage)
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('Vodafone Czech Republic',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(600, 600, 800, 800, 600, 1000)));

-- Nordic Telecom - Circle (moderate coverage)
INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY)
VALUES ('Nordic Telecom',
    SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 1003, 4),
    SDO_ORDINATE_ARRAY(100, 100, 300, 300, 100, 500)));


-- ============================================================================
-- BIKE ROUTES (Exclusively Multilinestrings)
-- ============================================================================

-- Route 1 - Downtown Connector (Multilinestring with 2 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Downtown Connector Path',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 11, 2, 1),
    SDO_ORDINATE_ARRAY(400, 400, 450, 450, 500, 500, 520, 520, 540, 540,
                       460, 460, 480, 480, 500, 500)));

-- Route 2 - Riverside Trail (Multilinestring with 2 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Svratka River Trail',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 13, 2, 1),
    SDO_ORDINATE_ARRAY(650, 550, 670, 570, 690, 590, 710, 610, 730, 630, 750, 650,
                       680, 580, 700, 600, 720, 620)));

-- Route 3 - University Circuit (Multilinestring with 3 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('University Campus Loop',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 9, 2, 1, 17, 2, 1),
    SDO_ORDINATE_ARRAY(550, 600, 570, 620, 590, 640, 610, 660,
                       600, 610, 620, 630, 640, 650, 660, 670,
                       580, 630, 600, 650, 620, 670)));

-- Route 4 - Exhibition Center Route (Multilinestring with 2 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Exhibition Center Bike Path',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 11, 2, 1),
    SDO_ORDINATE_ARRAY(100, 750, 120, 770, 140, 790, 160, 810, 180, 830,
                       130, 780, 150, 800, 170, 820, 190, 840)));

-- Route 5 - Tech Park Express (Multilinestring with 2 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Technology Park Express',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 15, 2, 1),
    SDO_ORDINATE_ARRAY(800, 150, 820, 160, 840, 170, 860, 180, 880, 190, 900, 200, 920, 210,
                       850, 200, 870, 210, 890, 220, 910, 230)));

-- Route 6 - Spilberk Hill Climb (Multilinestring with 2 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('Spilberk Castle Hill Route',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 13, 2, 1),
    SDO_ORDINATE_ARRAY(320, 620, 340, 640, 360, 660, 380, 680, 400, 700, 420, 720,
                       350, 650, 370, 670, 390, 690, 410, 710)));

-- Route 7 - Cross-City Corridor (Multilinestring with 3 segments)
INSERT INTO BIKE_ROUTES (NAME, GEOMETRY)
VALUES ('East-West City Corridor',
    SDO_GEOMETRY(2006, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1, 17, 2, 1, 29, 2, 1),
    SDO_ORDINATE_ARRAY(200, 500, 250, 500, 300, 500, 350, 500, 400, 500, 450, 500, 500, 500, 550, 500,
                       200, 480, 250, 480, 300, 480, 350, 480, 400, 480, 450, 480,
                       550, 500, 600, 500, 650, 500, 700, 500, 750, 500, 800, 500)));

COMMIT;
