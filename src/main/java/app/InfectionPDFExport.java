package app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import app.model.InfectionData;
import io.javalin.http.Handler;
import io.javalin.http.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class InfectionPDFExport implements Handler {
    
    private JDBCConnection connection;
    
    public InfectionPDFExport(JDBCConnection connection) {
        this.connection = connection;
    }
    
    public static final String URL = "/export/infection/pdf";
    
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
            
            // Generate PDF
            byte[] pdfBytes = generatePDF(infectionData, country, economicStatus, infectionType, yearStart, yearEnd);
            
            // Set response headers
            String fileName = generateFileName(country, economicStatus, infectionType, yearStart, yearEnd, "pdf");
            context.contentType("application/pdf");
            context.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            context.result(pdfBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            context.status(500).result("Error generating PDF: " + e.getMessage());
        }
    }
    
    private byte[] generatePDF(ArrayList<InfectionData> data, String country, String economicStatus, 
                             String infectionType, String yearStart, String yearEnd) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            
            // Colors
            PDColor grayColor = new PDColor(new float[]{0.78f, 0.78f, 0.78f}, PDDeviceRGB.INSTANCE);
            PDColor blackColor = new PDColor(new float[]{0f, 0f, 0f}, PDDeviceRGB.INSTANCE);
            
            // Create first content stream with try-with-resources
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Infection Data Export");
                contentStream.endText();
                yPosition -= 30;
                
                // Filter information
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                StringBuilder filterInfo = new StringBuilder("Filters Applied: ");
                if (country != null && !country.isEmpty()) filterInfo.append("Country: ").append(country).append("; ");
                if (economicStatus != null && !economicStatus.isEmpty()) filterInfo.append("Economic Status: ").append(economicStatus).append("; ");
                if (infectionType != null && !infectionType.isEmpty()) filterInfo.append("Infection Type: ").append(infectionType).append("; ");
                if (yearStart != null && !yearStart.isEmpty()) filterInfo.append("From: ").append(yearStart).append(" ");
                if (yearEnd != null && !yearEnd.isEmpty()) filterInfo.append("To: ").append(yearEnd).append("; ");
                contentStream.showText(filterInfo.toString());
                contentStream.endText();
                yPosition -= 30;
                
                // Table headers
                String[] headers = {"Year", "Country", "Economic Status", "Infection Type", "Cases"};
                float[] columnWidths = {40, 80, 100, 100, 60};
                float tableWidth = 0;
                for (float width : columnWidths) tableWidth += width;
                
                // Draw header background
                contentStream.setNonStrokingColor(grayColor);
                contentStream.addRect(margin, yPosition - 15, tableWidth, 20);
                contentStream.fill();
                contentStream.setNonStrokingColor(blackColor);
                
                // Header text
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                float xPosition = margin + 5;
                for (int i = 0; i < headers.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition, yPosition - 10);
                    contentStream.showText(headers[i]);
                    xPosition += columnWidths[i];
                    contentStream.endText();
                }
                yPosition -= 25;
                
                // Table data
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                
                for (int i = 0; i < data.size(); i++) {
                    InfectionData infection = data.get(i);
                    
                    // Check if we need a new page
                    if (yPosition < margin + 50) {
                        // Close current content stream before creating a new one
                       
                        
                        // Create new page and content stream
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        
                        // Create new content stream for the new page
                        try (PDPageContentStream newContentStream = new PDPageContentStream(document, page)) {
                            // Set up the new content stream
                            newContentStream.setFont(PDType1Font.HELVETICA, 8);
                            newContentStream.setNonStrokingColor(blackColor);
                            yPosition = yStart - 60;
                            
                            // Continue processing remaining data with the new content stream
                            for (int j = i; j < data.size(); j++) {
                                InfectionData remainingInfection = data.get(j);
                                
                                // Check if we need yet another page
                                if (yPosition < margin + 50) {
                                    break; // This would require additional pagination logic
                                }
                                
                                newContentStream.beginText();
                                float currentX = margin + 5;
                                
                                // Year
                                newContentStream.newLineAtOffset(currentX, yPosition);
                                newContentStream.showText(String.valueOf(remainingInfection.getYear()));
                                currentX += columnWidths[0];
                                
                                // Country
                                newContentStream.newLineAtOffset(currentX - (margin + 5), 0);
                                String countryText = remainingInfection.getCountry();
                                if (countryText.length() > 12) countryText = countryText.substring(0, 12) + "...";
                                newContentStream.showText(countryText);
                                currentX += columnWidths[1];
                                
                                // Economic Status
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0]), 0);
                                String economicText = remainingInfection.getEconomicStatus();
                                if (economicText.length() > 15) economicText = economicText.substring(0, 15) + "...";
                                newContentStream.showText(economicText);
                                currentX += columnWidths[2];
                                
                                // Infection Type
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1]), 0);
                                String infectionText = remainingInfection.getInfType();
                                if (infectionText.length() > 15) infectionText = infectionText.substring(0, 15) + "...";
                                newContentStream.showText(infectionText);
                                currentX += columnWidths[3];
                                
                                // Cases
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2]), 0);
                                newContentStream.showText(String.format("%.0f", remainingInfection.getCases()));
                                
                                newContentStream.endText();
                                
                                yPosition -= 15;
                                i = j; // Update the outer loop counter
                            }
                        }
                        break; // Exit the outer loop since we processed remaining items
                    }
                    
                    // Process current infection data
                    contentStream.beginText();
                    float currentX = margin + 5;
                    
                    // Year
                    contentStream.newLineAtOffset(currentX, yPosition);
                    contentStream.showText(String.valueOf(infection.getYear()));
                    currentX += columnWidths[0];
                    
                    // Country
                    contentStream.newLineAtOffset(currentX - (margin + 5), 0);
                    String countryText = infection.getCountry();
                    if (countryText.length() > 12) countryText = countryText.substring(0, 12) + "...";
                    contentStream.showText(countryText);
                    currentX += columnWidths[1];
                    
                    // Economic Status
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0]), 0);
                    String economicText = infection.getEconomicStatus();
                    if (economicText.length() > 15) economicText = economicText.substring(0, 15) + "...";
                    contentStream.showText(economicText);
                    currentX += columnWidths[2];
                    
                    // Infection Type
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1]), 0);
                    String infectionText = infection.getInfType();
                    if (infectionText.length() > 15) infectionText = infectionText.substring(0, 15) + "...";
                    contentStream.showText(infectionText);
                    currentX += columnWidths[3];
                    
                    // Cases
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2]), 0);
                    contentStream.showText(String.format("%.0f", infection.getCases()));
                    
                    contentStream.endText();
                    
                    yPosition -= 15;
                }
                
                // Summary (only if we're still on the first page)
                if (!data.isEmpty() && yPosition > margin + 50) {
                    yPosition -= 20;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Total Records: " + data.size());
                    contentStream.endText();
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
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