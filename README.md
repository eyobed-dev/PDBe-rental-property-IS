# Rental Property IS

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![Oracle](https://img.shields.io/badge/Oracle-Spatial%20%26%20Multimedia-red.svg)](https://docs.oracle.com/)

> **Course:** Advanced Database Systems (PDBe) @ FIT VUT Brno (2025/2026)  
> **Authors:** Eyobed Awel Nuri (`xnuriey00`), San Kaung (`xsankau00`)  
> **Repository:** [PDBe-rental-property-IS](https://github.com/eyobed-dev/PDBe-rental-property-IS)  
> **Topics:** `java`, `oracle-database`, `oracle-spatial`, `oracle-multimedia`, `swing-gui`, `cbir`, `gis`, `sdo-geometry`

An enterprise desktop application for managing rental properties and urban spatial infrastructure using **Java Swing**, **Oracle Spatial (`SDO_GEOMETRY`)**, and **Oracle Multimedia (`ORDImage`)**.

![Demo](Rental%20Property%20System/demo.png)

---

## Key Features

- **Interactive 2D Map:** Real-time vector rendering of properties, schools, transit stops, bike routes, and cellular coverage with interactive drawing tools (points, lines, polygons, circles).
- **Spatial Queries & Analytics:** Proximity search (`SDO_WITHIN_DISTANCE`), nearest neighbor (`SDO_NN`), boundary filtering (`SDO_ANYINTERACT`), and geometric metrics (area, length, centroid).
- **Multimedia Retrieval (CBIR):** Automated feature extraction (color histogram, average color, texture, positional color) and weighted similarity search (`SI_Score`) with image transformation tools (rotate, mirror, flip).
- **Relational Search & Live SQL Monitor:** Price limit and keyword search with a real-time audit console showing queries executed against Oracle.
- **Role-Based Access Control:** Admin and User roles with user management (CRUD) and in-app database reinitialization (`schema.sql`).

---

## Quick Start

### 1. Prerequisites
- **Java 21+** & **Apache Maven 3.8+**
- **Oracle Database 12c+** (with Spatial and Multimedia enabled)

### 2. Configure Database
Set your Oracle connection in [`Rental Property System/src/main/resources/db.properties`](Rental%20Property%20System/src/main/resources/db.properties):
```properties
db.url=jdbc:oracle:thin:@//<host>:<port>/<service>
db.username=<username>
db.password=<password>
```

### 3. Run Application
```bash
cd "Rental Property System"
./build-and-run.sh
```
*Alternatively, package a standalone executable JAR via `./package.sh` and run:*
```bash
java -jar target/rentalpropertysystem-1.0-SNAPSHOT.jar
```

---

## Default Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin` *(or `admin123`)* |
| **User** | `landlord1` | `pass123` |
