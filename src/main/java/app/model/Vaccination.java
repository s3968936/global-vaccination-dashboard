package app.model;

public class Vaccination {
    private String infType;
    private String antigen;
    private String country;
    private int year;
    private double targetNum;
    private double doses;
    private double coverage;

    public Vaccination() {}

    public Vaccination(String infType, String antigen, String country, int year,
                       double targetNum, double doses, double coverage) {
        this.infType = infType;
        this.antigen = antigen;
        this.country = country;
        this.year = year;
        this.targetNum = targetNum;
        this.doses = doses;
        this.coverage = coverage;
    }

    public String getInfType() { 
        return infType; 
    }
    
    public void setInfType(String infType) { 
        this.infType = infType; 
    }

    public String getAntigen() { 
        return antigen; 
    }
   
        public void setAntigen(String antigen) { 
        this.antigen = antigen; 
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

    public double getTargetNum() { 
        return targetNum; 
    }
    
        public void setTargetNum(double targetNum) { 
        this.targetNum = targetNum; 
    }

    public double getDoses() { 
        return doses; 
    }
    
    public void setDoses(double doses) { 
        this.doses = doses; 
    }

    public double getCoverage() { 
        return coverage; 
    }
    
    public void setCoverage(double coverage) { 
        this.coverage = coverage; 
    }

    @Override
    public String toString() {
        return "Vaccination{infType='" + infType + "', antigen='" + antigen + "', country='" + country + "', year=" + year +
               ", targetNum=" + targetNum + ", doses=" + doses + ", coverage=" + coverage + "}";
    }
}
