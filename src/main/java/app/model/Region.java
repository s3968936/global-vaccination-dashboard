package app.model;

public class Region {
    private String regionID;
    private String region;

    public Region() {}

    public Region(String regionID, String region) {
        this.regionID = regionID;
        this.region = region;
    }

    public String getRegionID() { 
        return regionID; 
    }
    
    public void setRegionID(String regionID) { 
        this.regionID = regionID;
    }

    public String getRegion() { 
        return region; 
    }
    
    public void setRegion(String region) { 
        this.region = region; 
    }

    @Override
    public String toString() {
        return "Region{regionID='" + regionID + "', region='" + region + "'}";
    }
}