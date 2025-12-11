# Rental Property Information System (Rental Property IS)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![Database](https://img.shields.io/badge/Database-Oracle%2012c%2F18c%2F19c-red.svg)](https://www.oracle.com/database/)
[![Oracle Spatial](https://img.shields.io/badge/Oracle-Spatial%20%26%20SDO-green.svg)](https://docs.oracle.com/database/121/SPATL/)
[![Oracle Multimedia](https://img.shields.io/badge/Oracle-Multimedia%20(ORDImage)-purple.svg)](https://docs.oracle.com/database/121/AIVUG/)

> **Course:** Advanced Database Systems (*Pokročilé databázové systémy* – PDBe)  
> **Academic Year:** 2025/2026  
> **Institution:** Brno University of Technology (BUT), Faculty of Information Technology (FIT)  
> **Team:** `xnuriey00`  
> **Authors:**  
> - Eyobed Awel Nuri (`xnuriey00`)  
> - San Kaung (`xsankau00` / `xkaungs00`)  
> **GitLab Repository:** [rental-property-is](https://gitlab.com/fit-vut1/pdbe/rental-property-is)

---

## Overview

The **Rental Property Information System** is an enterprise desktop application built with **Java Swing** and **Oracle Database**, designed for managing real estate inventory, municipal amenities, and urban infrastructure. 

The project demonstrates advanced database concepts including:
- **Oracle Spatial & Graph (`SDO_GEOMETRY`)**: 2D vector geometries, spatial indexing (`MDSYS.SPATIAL_INDEX_V2`), topological relationships (`SDO_ANYINTERACT`, `SDO_FILTER`), proximity queries (`SDO_WITHIN_DISTANCE`), K-Nearest Neighbors (`SDO_NN`), and geometric calculations (`SDO_GEOM.SDO_AREA`, `SDO_GEOM.SDO_LENGTH`, `SDO_GEOM.SDO_CENTROID`).
- **Oracle Multimedia (`ORDSYS.ORDImage`)**: Content-Based Image Retrieval (CBIR) utilizing image signatures (`ORDSYS.SI_StillImage`) with automated feature extraction via PL/SQL triggers (Average Color, Color Histogram, Positional Color, and Texture).
- **Interactive Geospatial Canvas**: Custom 2D vector map rendering properties, catchment areas, transit stops, bike routes, and cellular coverage.
- **Role-Based Access Control (RBAC)**: Distinct permissions for Administrators and Regular Users/Landlords, including user management and in-app database reinitialization.

---

## User Interface & Demo

![Rental Property IS User Interface](Rental%20Property%20System/demo.png)

The application interface is structured into three primary working areas:
1. **Left Panel (Multi-Modal Search & Results)**: Relational keyword and price filtering, spatial drawing and proximity searches, and multimedia image similarity scoring. The lower section contains an interactive entity results table.
2. **Center Panel (Interactive Map & SQL Console)**: Vector map rendering layers, interactive shape selection, layer visibility toggles, and a live SQL Query Log showing statements executed against Oracle.
3. **Right Panel (Entity Management & Analysis)**: Property details form, image gallery with transformations (rotate, mirror, flip), and spatial analytics calculations (area, perimeter, centroid).

---

## Core Features

### 1. Interactive 2D Map & Geometry Drawing
- **Multi-layer Geospatial Display**:
  - **Properties**: Polygons and circles representing apartments and estates.
  - **Schools**: Catchment polygons defining school districts.
  - **Transit Stops**: Point markers representing public transportation nodes.
  - **Cellular Coverage**: Circular and buffered coverage boundaries.
  - **Bike Routes**: Multi-segment line strings for cycling infrastructure.
- **Layer Controls**: Toggle visibility of Grid, Properties, Schools, Transit, Cellular Coverage, Bike Routes, and Search Overlays.
- **Vector Drawing Tools**: Interactive map drawing modes for Points, Lines, Polygons, Circles, and Polygons with Holes.

### 2. Spatial Querying & Analytics (Oracle Spatial)
- **Proximity Search (`SDO_WITHIN_DISTANCE`)**: Locate properties within a configurable distance (e.g., 500m) of schools, transit stops, or other infrastructure.
- **K-Nearest Neighbors (`SDO_NN`)**: Find the nearest transit stops, schools, or bike paths relative to a selected property.
- **Area Boundary Search (`SDO_ANYINTERACT` / `SDO_FILTER`)**: Draw custom polygons on the map to query all entities located within that region.
- **Geometric Calculations**:
  - `SDO_GEOM.SDO_AREA`: Computes polygon surface area.
  - `SDO_GEOM.SDO_LENGTH`: Computes perimeter length.
  - `SDO_GEOM.SDO_CENTROID`: Computes geometric center point coordinate.
- **Spatial Indexing**: All spatial tables utilize `MDSYS.SPATIAL_INDEX_V2` with coordinate bounds $[0, 1000]$ registered in `USER_SDO_GEOM_METADATA`.

### 3. Multimedia & Content-Based Image Retrieval (Oracle Multimedia)
- **Database-Driven Image Storage**: High-resolution property imagery stored as `ORDSYS.ORDImage`.
- **Automated Feature Extraction**: A PL/SQL database trigger automatically extracts Still Image features (`ORDSYS.SI_StillImage`) upon insert or update:
  - **AC**: `SI_AverageColor`
  - **CH**: `SI_ColorHistogram`
  - **PC**: `SI_PositionalColor`
  - **TX**: `SI_Texture`
- **Similarity Search (`SI_Score`)**: Search similar properties based on reference images using weighted feature scores:
  $$\text{Score} = w_{ac} \cdot \text{Score}_{ac} + w_{ch} \cdot \text{Score}_{ch} + w_{pc} \cdot \text{Score}_{pc} + w_{tx} \cdot \text{Score}_{tx}$$
- **Image Manipulation**: In-app on-the-fly transformations including horizontal mirroring, 90-degree clockwise rotation, and vertical flipping.

### 4. Relational Search & Property Management
- **Text & Budget Filtering**: Real-time filtering by building name, address, description, and maximum price slider.
- **Entity Management**: Create, view, edit, and delete properties and associated amenities.
- **Live SQL Query Monitor**: Audits and displays all raw SQL queries dispatched to Oracle, making spatial and multimedia operations transparent.

### 5. Role-Based Access Control (RBAC) & DB Administration
- **Authentication**: Secure login and user registration.
- **Roles**:
  - **Administrator (`1`)**: Access to user management (CRUD) and database schema reinitialization.
  - **Regular User / Landlord (`0`)**: Property listing management and search operations.
- **In-App Schema Reinitialization**: Admins can execute `schema.sql` directly from the UI to reset or refresh the database.

---

## Database Architecture

The schema utilizes specialized Oracle Object-Relational and Spatial types:

| Table | Description | Special Attributes / Types |
| :--- | :--- | :--- |
| `USERS` | Authentication & role management | `USER_ID`, `USERNAME`, `PASSWORD_HASH`, `USER_TYPE` |
| `PROPERTIES` | Real estate listings & geometries | `PRICE`, `NUM_BEDROOMS`, `SQUARE_METERS`, `GEOMETRY` (`SDO_GEOMETRY`) |
| `PROPERTY_IMAGES` | Multimedia assets & extracted features | `IMAGE_DATA` (`ORDImage`), `IMAGE_SI` (`SI_StillImage`), `IMAGE_AC`, `IMAGE_CH`, `IMAGE_PC`, `IMAGE_TX` |
| `SCHOOL_CATCHMENT_AREAS` | School districts & service boundaries | `GEOMETRY` (`SDO_GEOMETRY` – Polygon) |
| `TRANSIT_STOPS` | Public transport locations | `GEOMETRY` (`SDO_GEOMETRY` – Point) |
| `CELLULAR_COVERAGE` | Telecommunication coverage zones | `GEOMETRY` (`SDO_GEOMETRY` – Polygon / Buffer Circle) |
| `BIKE_ROUTES` | Cycling path infrastructure | `GEOMETRY` (`SDO_GEOMETRY` – LineString) |

---

## Getting Started

### Prerequisites
- **Java 21 JDK** or later
- **Apache Maven 3.8+**
- **Oracle Database 12c+** (with Oracle Spatial and Oracle Multimedia components enabled)

### Database Configuration

Connection details are configured in [`Rental Property System/src/main/resources/db.properties`](file:///home/eyobed/Documents/School/BUT/Semester%201/PDBe/PDBe%20Project/rental-property-is/Rental%20Property%20System/src/main/resources/db.properties):

```properties
db.url=jdbc:oracle:thin:@//<host>:<port>/<service_name>
db.username=<your_username>
db.password=<your_password>
```

> **Note:** Initial database tables and indexes can be created by running [`schema.sql`](file:///home/eyobed/Documents/School/BUT/Semester%201/PDBe/PDBe%20Project/rental-property-is/Rental%20Property%20System/src/main/resources/schema.sql) followed by [`data.sql`](file:///home/eyobed/Documents/School/BUT/Semester%201/PDBe/PDBe%20Project/rental-property-is/Rental%20Property%20System/src/main/resources/data.sql) to seed test data.

---

## Build & Execution

Navigate to the `Rental Property System` directory:

```bash
cd "Rental Property System"
```

### Using Provided Helper Scripts

| Script | Purpose | Command |
| :--- | :--- | :--- |
| `setup.sh` | Verifies Java/Maven environment and resolves dependencies | `./setup.sh` |
| `build.sh` | Compiles the project source code | `./build.sh` |
| `run.sh` | Launches the compiled application | `./run.sh` |
| `build-and-run.sh` | Compiles and immediately launches the application | `./build-and-run.sh` |
| `package.sh` | Packages the application into an executable JAR file | `./package.sh` |
| `clean.sh` | Removes target directory and build artifacts | `./clean.sh` |

### Running via Maven

To compile and launch directly:
```bash
mvn clean compile exec:java -Dexec.mainClass="cz.vut.fit.pdbe.xnuriey00.Main"
```

### Running the Packaged JAR

After running `./package.sh`, execute:
```bash
java -jar target/rentalpropertysystem-1.0-SNAPSHOT.jar
```

---

## Default Credentials

The initial seed data provides the following accounts:

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin` *(or `admin123`)* | Full administrative privileges, user management, DB reinitialization |
| **Regular User** | `landlord1` | `pass123` | Property management, search, and image operations |

*New users can also be registered directly through the application's login window.*

---

## Project Structure

```
rental-property-is/
├── .gitignore                                     # Root Git ignore rules (archives, IDE, temp files)
├── README.md                                      # Project documentation
├── Multimedia_photos/                             # Sample images for multimedia similarity testing
│   ├── vut.jpg
│   ├── vut2.jpg
│   └── vut3.jpg
├── reviews/                                       # Progress reports and reviews
│   └── 2025-11-30-review.md
└── Rental Property System/                        # Main Java application directory
    ├── pom.xml                                    # Maven configuration & course dependencies
    ├── demo.png                                   # System GUI demonstration screenshot
    ├── PDBe-User-Manual-latest.docx               # Full project documentation & user manual
    ├── SCRIPTS_README.md                          # Helper script usage reference
    ├── setup.sh                                   # Environment check & dependency setup
    ├── build.sh                                   # Compilation script
    ├── run.sh                                     # Application launch script
    ├── build-and-run.sh                           # Build and run script
    ├── package.sh                                 # Standalone JAR packaging script
    ├── clean.sh                                   # Build artifact cleanup script
    └── src/
        └── main/
            ├── java/cz/vut/fit/pdbe/xnuriey00/
            │   ├── Main.java                      # Application entry point
            │   ├── model/                         # Domain models (Property, BikeRoute, School, Transit, Cellular, User)
            │   ├── queries/                       # DAO layer (PropertyQueries, SpatialEntityQueries, UserQueries, etc.)
            │   ├── ui/                            # Swing UI (MainWindow, InitialWindow, UserManagementWindow)
            │   │   ├── components/                # SearchPanel, SpatialCalculationPanel, PropertyImagePanel
            │   │   ├── handlers/                  # Entity and map interaction handlers
            │   │   └── map/                       # Custom vector map canvas, tools, and rendering layers
            │   └── util/                          # DBConnection, SQLScriptExecutor, UserSession
            └── resources/
                ├── db.properties                  # Database connection credentials
                ├── schema.sql                     # Table definitions, triggers, and spatial metadata
                ├── data.sql                       # Sample dataset (properties, transit, bike routes, schools)
                └── logo.png                       # FIT logo icon
```

---

## Academic Context & Course Reference

- **Course:** Pokročilé databázové systémy (PDBe)
- **Institution:** Faculty of Information Technology, Brno University of Technology
- **Course Website:** [FIT BUT PDBe Course](https://www.fit.vut.cz/study/courses/PDBe/)
- **Course Educational Resources:** [Advanced Database Systems Repository](https://rychly-edu.gitlab.io/dbs/)
