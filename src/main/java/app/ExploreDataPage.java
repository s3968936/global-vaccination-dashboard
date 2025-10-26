package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import app.model.Vaccination;

public class ExploreDataPage implements Handler {

    private JDBCConnection connection;

    public ExploreDataPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/explore";
    private static final String TEMPLATE = "exploredata.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        try {
            // Get filter data from database
            ArrayList<HashMap<String, String>> countries = connection.getAllCountries();
            ArrayList<HashMap<String, String>> regions = connection.getAllRegions();
            ArrayList<HashMap<String, String>> antigens = connection.getAllAntigens();
            ArrayList<HashMap<String, String>> years = connection.getAllYears();

            // Convert to simple lists for dropdowns
            ArrayList<String> countryList = new ArrayList<>();
            ArrayList<String> regionList = new ArrayList<>();
            ArrayList<String> antigenList = new ArrayList<>();
            ArrayList<String> yearList = new ArrayList<>();

            for (HashMap<String, String> country : countries) {
                countryList.add(country.get("country"));
            }
            for (HashMap<String, String> region : regions) {
                regionList.add(region.get("region"));
            }
            for (HashMap<String, String> antigen : antigens) {
                antigenList.add(antigen.get("antigen"));
            }
            for (HashMap<String, String> year : years) {
                yearList.add(year.get("year"));
            }

            model.put("countries", countryList);
            model.put("regions", regionList);
            model.put("antigens", antigenList);
            model.put("years", yearList);

            // Get filter parameters from request
            String country = context.queryParam("country");
            String region = context.queryParam("region");
            String antigen = context.queryParam("antigen");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");

            // Check if filters are applied
            boolean hasFilters = (country != null && !country.isEmpty()) ||
                                 (region != null && !region.isEmpty()) ||
                                 (antigen != null && !antigen.isEmpty()) ||
                                 (yearStart != null && !yearStart.isEmpty()) ||
                                 (yearEnd != null && !yearEnd.isEmpty());

            model.put("hasFilters", hasFilters);

            if (hasFilters) {
                // Get filtered vaccination data
                ArrayList<Vaccination> vaccinationData = connection.getVaccinationData(country, region, antigen, yearStart, yearEnd);
                model.put("vaccinationData", vaccinationData);

                // Keep form selections
                model.put("selectedCountry", country);
                model.put("selectedRegion", region);
                model.put("selectedAntigen", antigen);
                model.put("selectedYearStart", yearStart);
                model.put("selectedYearEnd", yearEnd);

                // Prepare chart data
                if (!vaccinationData.isEmpty()) {
                    StringBuilder chartDataJson = new StringBuilder("[");
                    int validDataCount = 0;

                    for (Vaccination data : vaccinationData) {
                        int year = data.getYear();
                        double coverage = data.getCoverage();

                        // Only include valid coverage
                        if (coverage >= 0) {
                            if (validDataCount > 0) {
                                chartDataJson.append(",");
                            }

                            // Use JSON-friendly double quotes
                            chartDataJson.append("[\"")
                                         .append(year)
                                         .append("\", ")
                                         .append(coverage)
                                         .append("]");
                            validDataCount++;
                        }
                    }

                    chartDataJson.append("]");
                    model.put("chartDataJson", chartDataJson.toString());
                    model.put("hasChartData", validDataCount > 0);
                } else {
                    model.put("hasChartData", false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading data: " + e.getMessage());
        }

        context.render(TEMPLATE, model);
    }
}
