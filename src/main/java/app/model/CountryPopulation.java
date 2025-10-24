package app.model;

public class CountryPopulation {
    private String country;
    private int year;
    private double population;

    public CountryPopulation() {}

    public CountryPopulation(String country, int year, double population) {
        this.country = country;
        this.year = year;
        this.population = population;
    }

    public String getCountry() { 
        return country; 
    }
    
    public void setCountry(String country) { 
        this.country = country; }

    public int getYear() { 
        return year;
    }
    
    public void setYear(int year) { 
        this.year = year; 
    }

    public double getPopulation() { 
        return population;
    }
    
    public void setPopulation(double population) { 
        this.population = population; 
    }

    @Override
    public String toString() {
        return "CountryPopulation{country='" + country + "', year=" + year + ", population=" + population + "}";
    }
}
