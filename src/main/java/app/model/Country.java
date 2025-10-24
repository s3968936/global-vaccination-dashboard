package app.model;

public class Country {
    private String countryID;
    private String name;
    private String region;
    private int economy;

    public Country() {}

    public Country(String countryID, String name, String region, int economy) {
        this.countryID = countryID;
        this.name = name;
        this.region = region;
        this.economy = economy;
    }

    public String getCountryID() { 
        return countryID; 
    }
    
    public void setCountryID(String countryID) { 
        this.countryID = countryID; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getRegion() { 
        return region; 
    }
    
    public void setRegion(String region) { 
        this.region = region; 
    }

    public int getEconomy() { 
        return economy; 
    }
    
    public void setEconomy(int economy) { 
        this.economy = economy; 
    }

    @Override
    public String toString() {
        return "Country{countryID='" + countryID + "', name='" + name + "', region='" + region + "', economy=" + economy + "}";
    }
}
