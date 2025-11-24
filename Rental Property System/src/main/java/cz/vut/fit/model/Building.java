
package cz.vut.fit.model;

import oracle.spatial.geometry.JGeometry; // You'll need Oracle Spatial libraries in your classpath
import java.sql.SQLException;

public class Building {
    private int buildingId;
    private String buildingName;
    private String address;
    private JGeometry geometry; // Oracle's Java representation of SDO_GEOMETRY

    // Constructor for creating a new building (ID will be auto-generated)
    public Building(String buildingName, String address, JGeometry geometry) {
        this.buildingName = buildingName;
        this.address = address;
        this.geometry = geometry;
    }

    // Constructor for retrieving from DB (with ID)
    public Building(int buildingId, String buildingName, String address, JGeometry geometry) {
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.address = address;
        this.geometry = geometry;
    }

    // Getters
    public int getBuildingId() { return buildingId; }
    public String getBuildingName() { return buildingName; }
    public String getAddress() { return address; }
    public JGeometry getGeometry() { return geometry; }

    // Setters (if you need to modify existing objects before update)
    public void setBuildingId(int buildingId) { this.buildingId = buildingId; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public void setAddress(String address) { this.address = address; }
    public void setGeometry(JGeometry geometry) { this.geometry = geometry; }

    @Override
    public String toString() {
        return "Building{" +
               "buildingId=" + buildingId +
               ", buildingName='" + buildingName + '\'' +
               ", address='" + address + '\'' +
               ", geometry=" + (geometry != null ? geometry.toString() : "null") + // JGeometry has a toString
               '}';
    }
}