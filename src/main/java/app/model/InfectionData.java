package app.model;

public class InfectionData {
    private String infType;
    private String country;
    private int year;
    private double cases;

    public InfectionData() {}

    public InfectionData(String infType, String country, int year, double cases) {
        this.infType = infType;
        this.country = country;
        this.year = year;
        this.cases = cases;
    }

    public String getInfType() { 
        return infType; 
    }
    
    public void setInfType(String infType) { 
        this.infType = infType; 
    }

    public String getCountry() { 
        return country; 
    }
    
    public void setCountry(String country) { 
        this.country = country; 
    }

    public int getYear() { 
        return year; 
    }
    
    public void setYear(int year) { 
        this.year = year; 
    }

    public double getCases() { 
        return cases; 
    }
    
    public void setCases(double cases) { 
        this.cases = cases; 
    }

    @Override
    public String toString() {
        return "InfectionData{infType='" + infType + "', country='" + country + "', year=" + year + ", cases=" + cases + "}";
    }
}
