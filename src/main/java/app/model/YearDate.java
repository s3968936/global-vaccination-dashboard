package app.model;

public class YearDate {
    private int yearID;

    public YearDate() {}

    public YearDate(int yearID) {
        this.yearID = yearID;
    }

    public int getYearID() { 
        return yearID; 
    }
    
    public void setYearID(int yearID) { 
        this.yearID = yearID; 
    }

    @Override
    public String toString() {
        return "YearDate{yearID=" + yearID + "}";
    }
}
