package cz.vut.fit.pdbe.xnuriey00.model;

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
    private String wktGeometry; // Well-Known Text

    public Property(Long propertyId, String buildingName, String address, String unitNumber, 
                    Double price, Integer numBedrooms, Double squareMeters, 
                    String description, Long listingUserId, String wktGeometry) {
        this.propertyId = propertyId;
        this.buildingName = buildingName;
        this.address = address;
        this.unitNumber = unitNumber;
        this.price = price;
        this.numBedrooms = numBedrooms;
        this.squareMeters = squareMeters;
        this.description = description;
        this.listingUserId = listingUserId;
        this.wktGeometry = wktGeometry;
    }

    public Property(String buildingName, String address, String unitNumber, 
                    Double price, Integer numBedrooms, Double squareMeters, 
                    String description, Long listingUserId, String wktGeometry) {
        this(null, buildingName, address, unitNumber, price, numBedrooms, squareMeters, description, listingUserId, wktGeometry);
    }

    // Getters
    public Long getPropertyId() { return propertyId; }
    public String getBuildingName() { return buildingName; }
    public String getAddress() { return address; }
    public String getUnitNumber() { return unitNumber; }
    public Double getPrice() { return price; }
    public Integer getNumBedrooms() { return numBedrooms; }
    public Double getSquareMeters() { return squareMeters; }
    public String getDescription() { return description; }
    public Long getListingUserId() { return listingUserId; }
    public String getWktGeometry() { return wktGeometry; }

    // Setters
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public void setWktGeometry(String wktGeometry) { this.wktGeometry = wktGeometry; }
}