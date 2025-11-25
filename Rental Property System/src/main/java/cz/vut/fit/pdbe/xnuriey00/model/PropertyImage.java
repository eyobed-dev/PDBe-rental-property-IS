package cz.vut.fit.pdbe.xnuriey00.model;

public package cz.vut.fit.pdbe.xnuriey00.model;

import java.sql.Date;

public class PropertyImage {
    private Long imageId;
    private Long propertyId;
    private String imageName;
    private Date uploadDate;

    public PropertyImage(Long imageId, Long propertyId, String imageName, Date uploadDate) {
        this.imageId = imageId;
        this.propertyId = propertyId;
        this.imageName = imageName;
        this.uploadDate = uploadDate;
    }

    public Long getImageId() { return imageId; }
    public Long getPropertyId() { return propertyId; }
    public String getImageName() { return imageName; }
    public Date getUploadDate() { return uploadDate; }
    
    @Override
    public String toString() {
        return imageName + " (" + uploadDate + ")";
    }
} {
    
}
