package app;
import app.model.InfectionData;
import io.javalin.http.Handler;
import io.javalin.http.Context;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class InfectionCSVExport implements Handler {
    
    private JDBCConnection connection;
    
    public InfectionCSVExport(JDBCConnection connection) {
        this.connection = connection;
    }
    
    public static final String URL = "/export/infection/csv";
    
    @Override
    public void handle(Context context) throws Exception {
        try {
            // Get filter parameters
            String country = context.queryParam("country");
            String economicStatus = context.queryParam("economicStatus");
            String infectionType = context.queryParam("infectionType");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");
            
            // Get filtered infection data
            ArrayList<InfectionData> infectionData = connection.getInfectionData(
                infectionType, economicStatus, country, yearStart, yearEnd);
            
            // Generate CSV
            byte[] csvBytes = generateCSV(infectionData, country, economicStatus, infectionType, yearStart, yearEnd);
            
            // Set response headers
            String fileName = generateFileName(country, economicStatus, infectionType, yearStart, yearEnd, "csv");
            context.contentType("text/csv");
            context.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            context.result(csvBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            context.status(500).result("Error generating CSV: " + e.getMessage());
        }
    }
    
    private byte[] generateCSV(ArrayList<InfectionData> data, String country, String economicStatus, 
                             String infectionType, String yearStart, String yearEnd) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
             PrintWriter writer = new PrintWriter(outputStreamWriter)) {
            
            // Write CSV header
            writer.println("Year,Country,Economic Status,Infection Type,Cases");
            
            // Write data rows
            for (InfectionData infection : data) {
                writer.printf("%d,%s,%s,%s,%.0f%n",
                    infection.getYear(),
                    escapeCsvField(infection.getCountry()),
                    escapeCsvField(infection.getEconomicStatus()),
                    escapeCsvField(infection.getInfType()),
                    infection.getCases());
            }
            
            // Write summary
            writer.println();
            writer.println("Summary:");
            writer.println("Total Records," + data.size());
            writer.println("Filters Applied," + buildFilterInfo(country, economicStatus, infectionType, yearStart, yearEnd));
            
            writer.flush();
            return outputStream.toByteArray();
        }
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "";
        // Escape fields that contain commas, quotes, or newlines
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private String buildFilterInfo(String country, String economicStatus, String infectionType, 
                                 String yearStart, String yearEnd) {
        StringBuilder filterInfo = new StringBuilder();
        if (country != null && !country.isEmpty()) filterInfo.append("Country: ").append(country).append("; ");
        if (economicStatus != null && !economicStatus.isEmpty()) filterInfo.append("Economic Status: ").append(economicStatus).append("; ");
        if (infectionType != null && !infectionType.isEmpty()) filterInfo.append("Infection Type: ").append(infectionType).append("; ");
        if (yearStart != null && !yearStart.isEmpty()) filterInfo.append("From: ").append(yearStart).append("; ");
        if (yearEnd != null && !yearEnd.isEmpty()) filterInfo.append("To: ").append(yearEnd).append("; ");
        return filterInfo.toString();
    }
    
    private String generateFileName(String country, String economicStatus, String infectionType, 
                                  String yearStart, String yearEnd, String extension) {
        StringBuilder fileName = new StringBuilder("infection_data");
        
        if (country != null && !country.isEmpty()) fileName.append("_").append(country.replace(" ", "_"));
        if (economicStatus != null && !economicStatus.isEmpty()) fileName.append("_").append(economicStatus.replace(" ", "_"));
        if (infectionType != null && !infectionType.isEmpty()) fileName.append("_").append(infectionType.replace(" ", "_"));
        if (yearStart != null && !yearStart.isEmpty()) fileName.append("_from_").append(yearStart);
        if (yearEnd != null && !yearEnd.isEmpty()) fileName.append("_to_").append(yearEnd);
        
        fileName.append(".").append(extension);
        return fileName.toString();
    }
}
