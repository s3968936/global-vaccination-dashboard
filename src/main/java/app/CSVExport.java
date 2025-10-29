package app;

import app.model.Vaccination;
import io.javalin.http.Handler;
import io.javalin.http.Context;

import java.util.ArrayList;

public class CSVExport implements Handler {
    
    private JDBCConnection connection;
    
    public CSVExport(JDBCConnection connection) {
        this.connection = connection;
    }
    
    public static final String URL = "/export/csv";
    
    @Override
    public void handle(Context context) throws Exception {
        try {
            // Get the same filters as in ExploreDataPage
            String country = context.queryParam("country");
            String region = context.queryParam("region");
            String antigen = context.queryParam("antigen");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");
            
            // Get filtered data
            ArrayList<Vaccination> vaccinationData = connection.getVaccinationData(
                country, region, antigen, yearStart, yearEnd);
            
            // Generate CSV
            String csvContent = generateCSV(vaccinationData, country, region, antigen, yearStart, yearEnd);
            
            // Set response headers for CSV download
            String fileName = generateFileName(country, region, antigen, yearStart, yearEnd, "csv");
            context.contentType("text/csv");
            context.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            context.result(csvContent);
            
        } catch (Exception e) {
            e.printStackTrace();
            context.status(500).result("Error generating CSV: " + e.getMessage());
        }
    }
    
    private String generateCSV(ArrayList<Vaccination> data, String country, String region, 
                             String antigen, String yearStart, String yearEnd) {
        StringBuilder csv = new StringBuilder();
        
        // Add header with filter information
        csv.append("Vaccination Data Export\n");
        csv.append("Filters Applied: ");
        if (country != null && !country.isEmpty()) csv.append("Country: ").append(country).append("; ");
        if (region != null && !region.isEmpty()) csv.append("Region: ").append(region).append("; ");
        if (antigen != null && !antigen.isEmpty()) csv.append("Antigen: ").append(antigen).append("; ");
        if (yearStart != null && !yearStart.isEmpty()) csv.append("From: ").append(yearStart).append("; ");
        if (yearEnd != null && !yearEnd.isEmpty()) csv.append("To: ").append(yearEnd).append("; ");
        csv.append("\n\n");
        
        // CSV headers
        csv.append("Year,Country,Antigen,Coverage (%),Target Population,Doses Administered\n");
        
        // Data rows
        for (Vaccination vaccination : data) {
            csv.append(vaccination.getYear()).append(",");
            csv.append(escapeCsvField(vaccination.getCountry())).append(",");
            csv.append(escapeCsvField(vaccination.getAntigen())).append(",");
            csv.append(String.format("%.2f", vaccination.getCoverage())).append(",");
            csv.append(String.format("%.0f", vaccination.getTargetNum())).append(",");
            csv.append(String.format("%.0f", vaccination.getDoses())).append("\n");
        }
        
        // Summary
        if (!data.isEmpty()) {
            csv.append("\n");
            csv.append("Total Records:,").append(data.size()).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "";
        // Escape quotes and wrap in quotes if contains comma or quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private String generateFileName(String country, String region, String antigen, 
                                  String yearStart, String yearEnd, String extension) {
        StringBuilder fileName = new StringBuilder("vaccination_data");
        
        if (country != null && !country.isEmpty()) fileName.append("_").append(country.replace(" ", "_"));
        if (region != null && !region.isEmpty()) fileName.append("_").append(region.replace(" ", "_"));
        if (antigen != null && !antigen.isEmpty()) fileName.append("_").append(antigen.replace(" ", "_"));
        if (yearStart != null && !yearStart.isEmpty()) fileName.append("_from_").append(yearStart);
        if (yearEnd != null && !yearEnd.isEmpty()) fileName.append("_to_").append(yearEnd);
        
        fileName.append(".").append(extension);
        return fileName.toString();
    }
}