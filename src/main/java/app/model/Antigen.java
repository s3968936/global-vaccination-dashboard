package app.model;

public class Antigen {
    private String antigenID;
    private String name;

    public Antigen() {}

    public Antigen(String antigenID, String name) {
        this.antigenID = antigenID;
        this.name = name;
    }

    public String getAntigenID() { 
        return antigenID; 
    }
    
    public void setAntigenID(String antigenID) { 
        this.antigenID = antigenID; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    @Override
    public String toString() {
        return "Antigen{antigenID='" + antigenID + "', name='" + name + "'}";
    }
}
