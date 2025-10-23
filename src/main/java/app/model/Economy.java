package app.model;

public class Economy {
    private int economyID;
    private String phase;

    public Economy() {}

    public Economy(int economyID, String phase) {
        this.economyID = economyID;
        this.phase = phase;
    }

    public int getEconomyID() { 
        return economyID; 
    }
    
    public void setEconomyID(int economyID) { 
        this.economyID = economyID; 
    }

    public String getPhase() { 
        return phase; 
    }
    
    public void setPhase(String phase) { 
        this.phase = phase; 
    }
    
    @Override
    public String toString() {
        return "Economy{economyID=" + economyID + ", phase='" + phase + "'}";
    }
}
