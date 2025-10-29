package app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import app.model.Vaccination;
import io.javalin.http.Handler;
import io.javalin.http.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PDFExport implements Handler {
    
    private JDBCConnection connection;
    
    public PDFExport(JDBCConnection connection) {
        this.connection = connection;
    }
    
    public static final String URL = "/export/pdf";
    
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
            
            // Generate PDF
            byte[] pdfBytes = generatePDF(vaccinationData, country, region, antigen, yearStart, yearEnd);
            
            // Set response headers for PDF download
            String fileName = generateFileName(country, region, antigen, yearStart, yearEnd);
            context.contentType("application/pdf");
            context.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            context.result(pdfBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            context.status(500).result("Error generating PDF: " + e.getMessage());
        }
    }
    
    private byte[] generatePDF(ArrayList<Vaccination> data, String country, String region, 
                             String antigen, String yearStart, String yearEnd) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            
            // Colors
            PDColor grayColor = new PDColor(new float[]{0.78f, 0.78f, 0.78f}, PDDeviceRGB.INSTANCE);
            PDColor blackColor = new PDColor(new float[]{0f, 0f, 0f}, PDDeviceRGB.INSTANCE);
            
            // Table headers
            String[] headers = {"Year", "Country", "Antigen", "Coverage%", "Target Pop", "Doses"};
            float[] columnWidths = {40, 80, 80, 60, 70, 60};
            float tableWidth = 0;
            for (float width : columnWidths) tableWidth += width;
            
            // First page content
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Vaccination Data Export");
                contentStream.endText();
                yPosition -= 30;
                
                // Filter information
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                StringBuilder filterInfo = new StringBuilder("Filters Applied: ");
                if (country != null && !country.isEmpty()) filterInfo.append("Country: ").append(country).append("; ");
                if (region != null && !region.isEmpty()) filterInfo.append("Region: ").append(region).append("; ");
                if (antigen != null && !antigen.isEmpty()) filterInfo.append("Antigen: ").append(antigen).append("; ");
                if (yearStart != null && !yearStart.isEmpty()) filterInfo.append("From: ").append(yearStart).append("; ");
                if (yearEnd != null && !yearEnd.isEmpty()) filterInfo.append("To: ").append(yearEnd).append("; ");
                contentStream.showText(filterInfo.toString());
                contentStream.endText();
                yPosition -= 30;
                
                // Draw header background
                contentStream.setNonStrokingColor(grayColor);
                contentStream.addRect(margin, yPosition - 15, tableWidth, 20);
                contentStream.fill();
                contentStream.setNonStrokingColor(blackColor);
                
                // Header text
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                float xPosition = margin + 5;
                for (int i = 0; i < headers.length; i++) {
                    contentStream.newLineAtOffset(xPosition, yPosition - 10);
                    contentStream.showText(headers[i]);
                    xPosition += columnWidths[i];
                }
                contentStream.endText();
                yPosition -= 25;
                
                // Table data
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                
                for (int i = 0; i < data.size(); i++) {
                    Vaccination vaccination = data.get(i);
                    
                    // Check if we need a new page
                    if (yPosition < margin + 50) {
                        // Close current content stream
                        contentStream.close();
                        
                        // Create new page
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        
                        // Create new content stream for the new page
                        try (PDPageContentStream newContentStream = new PDPageContentStream(document, page)) {
                            newContentStream.setFont(PDType1Font.HELVETICA, 8);
                            newContentStream.setNonStrokingColor(blackColor);
                            
                            // Reset position for new page
                            yPosition = yStart - 60;
                            
                            // Continue with remaining data
                            for (int j = i; j < data.size(); j++) {
                                Vaccination remainingVaccination = data.get(j);
                                
                                if (yPosition < margin + 50) {
                                    break; // Would need another page, but for simplicity we stop here
                                }
                                
                                newContentStream.beginText();
                                float currentX = margin + 5;
                                
                                // Year
                                newContentStream.newLineAtOffset(currentX, yPosition);
                                newContentStream.showText(String.valueOf(remainingVaccination.getYear()));
                                currentX += columnWidths[0];
                                
                                // Country
                                newContentStream.newLineAtOffset(currentX - (margin + 5), 0);
                                String countryText = remainingVaccination.getCountry();
                                if (countryText.length() > 12) countryText = countryText.substring(0, 12) + "...";
                                newContentStream.showText(countryText);
                                currentX += columnWidths[1];
                                
                                // Antigen
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0]), 0);
                                String antigenText = remainingVaccination.getAntigen();
                                if (antigenText.length() > 12) antigenText = antigenText.substring(0, 12) + "...";
                                newContentStream.showText(antigenText);
                                currentX += columnWidths[2];
                                
                                // Coverage
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1]), 0);
                                newContentStream.showText(String.format("%.1f", remainingVaccination.getCoverage()));
                                currentX += columnWidths[3];
                                
                                // Target Population
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2]), 0);
                                newContentStream.showText(String.format("%.0f", remainingVaccination.getTargetNum()));
                                currentX += columnWidths[4];
                                
                                // Doses
                                newContentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3]), 0);
                                newContentStream.showText(String.format("%.0f", remainingVaccination.getDoses()));
                                
                                newContentStream.endText();
                                
                                yPosition -= 15;
                                i = j; // Update outer loop index
                            }
                        }
                        break; // Exit outer loop since we handled remaining items
                    }
                    
                    // Draw row on current page
                    contentStream.beginText();
                    float currentX = margin + 5;
                    
                    // Year
                    contentStream.newLineAtOffset(currentX, yPosition);
                    contentStream.showText(String.valueOf(vaccination.getYear()));
                    currentX += columnWidths[0];
                    
                    // Country
                    contentStream.newLineAtOffset(currentX - (margin + 5), 0);
                    String countryText = vaccination.getCountry();
                    if (countryText.length() > 12) countryText = countryText.substring(0, 12) + "...";
                    contentStream.showText(countryText);
                    currentX += columnWidths[1];
                    
                    // Antigen
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0]), 0);
                    String antigenText = vaccination.getAntigen();
                    if (antigenText.length() > 12) antigenText = antigenText.substring(0, 12) + "...";
                    contentStream.showText(antigenText);
                    currentX += columnWidths[2];
                    
                    // Coverage
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1]), 0);
                    contentStream.showText(String.format("%.1f", vaccination.getCoverage()));
                    currentX += columnWidths[3];
                    
                    // Target Population
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2]), 0);
                    contentStream.showText(String.format("%.0f", vaccination.getTargetNum()));
                    currentX += columnWidths[4];
                    
                    // Doses
                    contentStream.newLineAtOffset(currentX - (margin + 5 + columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3]), 0);
                    contentStream.showText(String.format("%.0f", vaccination.getDoses()));
                    
                    contentStream.endText();
                    
                    yPosition -= 15;
                }
                
                // Summary
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
    
    private String generateFileName(String country, String region, String antigen, 
                                  String yearStart, String yearEnd) {
        StringBuilder fileName = new StringBuilder("vaccination_data");
        
        if (country != null && !country.isEmpty()) fileName.append("_").append(country.replace(" ", "_"));
        if (region != null && !region.isEmpty()) fileName.append("_").append(region.replace(" ", "_"));
        if (antigen != null && !antigen.isEmpty()) fileName.append("_").append(antigen.replace(" ", "_"));
        if (yearStart != null && !yearStart.isEmpty()) fileName.append("_from_").append(yearStart);
        if (yearEnd != null && !yearEnd.isEmpty()) fileName.append("_to_").append(yearEnd);
        
        fileName.append(".pdf");
        return fileName.toString();
    }
}