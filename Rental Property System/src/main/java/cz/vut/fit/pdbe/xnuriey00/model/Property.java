package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

/**
 * Represents a rental property entity.
 * 
 * @author eyobed-dev
 */
public class Property {
    private Long propertyId;
    private String buildingName;
    private String address;
    private String unitNumber;
    private Double price;
    private Integer numBedrooms;
    private Double squareMeters;
    private String description;
    private Long listingUserId;
    private JGeometry jGeometry;

    public Property(Long propertyId, String buildingName, String address, String unitNumber, Double price,
            Integer numBedrooms, Double squareMeters, String description, Long listingUserId, JGeometry jGeometry) {
        this.propertyId = propertyId;
        this.buildingName = buildingName;
        this.address = address;
        this.unitNumber = unitNumber;
        this.price = price;
        this.numBedrooms = numBedrooms;
        this.squareMeters = squareMeters;
        this.description = description;
        this.listingUserId = listingUserId;
        this.jGeometry = jGeometry;
    }

    public Property() {
    }

    // Getters
    public Long getPropertyId() {
        return propertyId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getAddress() {
        return address;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getNumBedrooms() {
        return numBedrooms;
    }

    public Double getSquareMeters() {
        return squareMeters;
    }

    public String getDescription() {
        return description;
    }

    public Long getListingUserId() {
        return listingUserId;
    }

    public JGeometry getJGeometry() {
        return jGeometry;
    }

    // Setters
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setNumBedrooms(Integer numBedrooms) {
        this.numBedrooms = numBedrooms;
    }

    public void setSquareMeters(Double squareMeters) {
        this.squareMeters = squareMeters;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setListingUserId(Long listingUserId) {
        this.listingUserId = listingUserId;
    }

    public void setJGeometry(JGeometry jGeometry) {
        this.jGeometry = jGeometry;
    }
}