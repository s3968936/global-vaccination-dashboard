package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.model.InfectionData;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class TrendingPage implements Handler {

    private JDBCConnection connection;

    public TrendingPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/trending";
    private static final String TEMPLATE = "trending.html";


    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("title", "Trending Health Data");

        try {
            // Get filter data from database
            ArrayList<String> infectionTypes = connection.getInfectionTypes();
            ArrayList<String> economicStatuses = connection.getEconomicStatuses();
            ArrayList<String> countries = connection.getCountries();
            ArrayList<String> years = connection.getYears();

            model.put("infectionTypes", infectionTypes);
            model.put("economicStatuses", economicStatuses);
            model.put("countries", countries);
            model.put("years", years);

            // Get filter parameters from request
            String economicStatus = context.queryParam("economicStatus");
            String infectionType = context.queryParam("infectionType");
            String country = context.queryParam("country");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");

            // Store selected filter values to repopulate form
            model.put("selectedEconomicStatus", economicStatus);
            model.put("selectedInfectionType", infectionType);
            model.put("selectedCountry", country);
            model.put("selectedYearStart", yearStart);
            model.put("selectedYearEnd", yearEnd);

            // Year range validation
            int minYear = 2000;
            int maxYear = 2024;
            boolean validYearRange = true;
            String warningMessage = null;

            try {
                if (yearStart != null && !yearStart.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    if (start < minYear || start > maxYear) {
                        validYearRange = false;
                        warningMessage = "⚠️ Start year must be between " + minYear + " and " + maxYear + ".";
                    }
                }
                if (yearEnd != null && !yearEnd.isEmpty()) {
                    int end = Integer.parseInt(yearEnd);
                    if (end < minYear || end > maxYear) {
                        validYearRange = false;
                        warningMessage = "⚠️ End year must be between " + minYear + " and " + maxYear + ".";
                    }
                }

                if (yearStart != null && !yearStart.isEmpty() && yearEnd != null && !yearEnd.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    int end = Integer.parseInt(yearEnd);
                    if (start > end) {
                        validYearRange = false;
                        warningMessage = "⚠️ Start year cannot be greater than end year.";
                    }
                }
            } catch (NumberFormatException e) {
                validYearRange = false;
                warningMessage = "⚠️ Please enter valid numeric years.";
            }

            if (warningMessage != null) {
                model.put("warning", warningMessage);
            }

            // Only query data if we have at least economic status and infection type, and valid years
            if (validYearRange && economicStatus != null && !economicStatus.isEmpty() &&
                infectionType != null && !infectionType.isEmpty()) {

                // Get detailed infection data filtered by economic status
                ArrayList<InfectionData> detailedData = connection.getInfectionData(
                    infectionType, economicStatus, country, yearStart, yearEnd
                );
                model.put("detailedData", detailedData);

                // Get aggregated summary data by economic status
                ArrayList<HashMap<String, String>> summaryData =
                    connection.getInfectionDataByEconomicStatus(infectionType, yearStart, yearEnd);
                model.put("summaryData", summaryData);

                // Prepare summary chart data as JSON
                StringBuilder chartJson = new StringBuilder("[");
                for (int i = 0; i < summaryData.size(); i++) {
                    HashMap<String, String> row = summaryData.get(i);
                    if (i > 0) chartJson.append(",");

                    // Remove commas from total_cases and avg_cases for chart
                    String totalCasesStr = row.get("total_cases").replace(",", "");
                    String avgCasesStr = row.get("avg_cases").replace(",", "");

                    chartJson.append("{")
                        .append("\"economic_status\":\"").append(row.get("economic_status")).append("\",")
                        .append("\"total_cases\":").append(totalCasesStr).append(",")
                        .append("\"avg_cases\":").append(avgCasesStr)
                        .append("}");
                }
                chartJson.append("]");

                model.put("chartDataJson", chartJson.toString());

                // Prepare detailed data as JSON for additional charts
                StringBuilder detailedJson = new StringBuilder("[");
                for (int i = 0; i < detailedData.size(); i++) {
                    InfectionData data = detailedData.get(i);
                    if (i > 0) detailedJson.append(",");

                    // Escape country names for JSON
                    String countryName = data.getCountry().replace("\"", "\\\"");

                    detailedJson.append("{")
                        .append("\"country\":\"").append(countryName).append("\",")
                        .append("\"year\":").append(data.getYear()).append(",")
                        .append("\"cases\":").append(data.getCases())
                        .append("}");
                }
                detailedJson.append("]");

                model.put("detailedDataJson", detailedJson.toString());
            }

        } catch (Exception e) {
            model.put("error", "Error loading trending data: " + e.getMessage());
            e.printStackTrace();
        }

        context.render(TEMPLATE, model);
    }
}