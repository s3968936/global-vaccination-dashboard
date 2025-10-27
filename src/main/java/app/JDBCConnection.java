package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import app.model.Persona;
import app.model.Vaccination;
import app.model.InfectionData;

/**
 * Class for Managing the JDBC Connection to a SQLLite Database.
 */
public class JDBCConnection {

    // Update this to your actual database path
    private static final String DATABASE = "jdbc:sqlite:database/who.db";

    public JDBCConnection() {
        System.out.println("Created JDBC Connection Object");
    }

    /**
     * Generic method to execute any SQL query and return results as ArrayList<HashMap>
     */
    public ArrayList<HashMap<String, String>> executeQuery(String query) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // Get Result
            ResultSet resultSet = statement.executeQuery(query);

            // Get metadata to know column names
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Process all of the results
            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    row.put(columnName, columnValue);
                }
                
                results.add(row);
            }

            // Close the statement
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    // Specific methods for your queries (optional - you can use executeQuery directly)
    
    public ArrayList<HashMap<String, String>> getTopVaccinationsByCoverage() {
        String query = "SELECT " +
                    "c.name as country_name, " +
                    "a.name as vaccine_name, " +
                    "ROUND((v.doses / v.target_num) * 10, 2) as coverage_percentage " +
                    "FROM Vaccination v " +
                    "JOIN Country c ON v.country = c.CountryID " +
                    "JOIN Antigen a ON v.antigen = a.AntigenID " +
                    "WHERE v.doses IS NOT NULL " +
                    "  AND v.target_num IS NOT NULL " +
                    "  AND v.target_num > 0 " +
                    "ORDER BY coverage_percentage DESC " +
                    "LIMIT 5";
        
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getEconomySnapshot() {
        String query = "SELECT e.phase AS economy, COUNT(c.CountryID) AS country_count, " +
                      "AVG(v.coverage) AS avg_vaccination " +
                      "FROM Economy e " +
                      "JOIN Country c ON e.economyID = c.economy " +
                      "LEFT JOIN Vaccination v ON c.CountryID = v.country " +
                      "GROUP BY e.phase";
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getRegions() {
        String query = "SELECT r.region, COUNT(c.CountryID) as country_count " +
                    "FROM Region r " +
                    "LEFT JOIN Country c ON r.RegionID = c.region " +
                    "GROUP BY r.region " +
                    "ORDER BY country_count DESC " +
                    "LIMIT 5";
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getTopInfections() {
        String query = "SELECT it.description AS infection_type, SUM(id.cases) AS total_cases " +
                      "FROM InfectionData id " +
                      "JOIN Infection_Type it ON id.inf_type = it.id " +
                      "GROUP BY it.description " +
                      "ORDER BY total_cases DESC LIMIT 10";
        return executeQuery(query);
    }
// FILTER DROPDOWN METHODS FOR EXPLOREDATA PAGE (VACCINATION)
    // Get all unique countries for the country filter dropdown
    public ArrayList<HashMap<String, String>> getAllCountries() {
        String query = "SELECT DISTINCT name as country FROM Country ORDER BY name";
        return executeQuery(query);
    }

    //Get all unique regions for the region filter dropdown
    public ArrayList<HashMap<String, String>> getAllRegions() {
        String query = "SELECT DISTINCT region as region FROM Region ORDER BY region";
        return executeQuery(query);
    }

    //Get all unique antigens for the antigen filter dropdown
    public ArrayList<HashMap<String, String>> getAllAntigens() {
        String query = "SELECT DISTINCT name as antigen FROM Antigen ORDER BY name";
        return executeQuery(query);
    }

    //Get all unique years for the year filter dropdown
    public ArrayList<HashMap<String, String>> getAllYears() {
        String query = "SELECT DISTINCT year as year FROM Vaccination ORDER BY year";
        return executeQuery(query);
    }

    //Get all the fields from the persona table in the database
    public ArrayList<Persona> getAllPersonas() {
        ArrayList<Persona> list = new ArrayList<>();
        String query = "SELECT * FROM Personas ORDER BY persona_id;";

        try (Connection conn = DriverManager.getConnection(DATABASE);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Persona p = new Persona(
                    rs.getInt("persona_id"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("occupation"),
                    rs.getString("education"),
                    rs.getString("location"),
                    rs.getString("language"),
                    rs.getString("disability"),
                    rs.getString("needs"),
                    rs.getString("goals"),
                    rs.getString("skills"),
                    rs.getString("image"),
                    rs.getString("image_credit")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DATABASE);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//===========================
    // Add methods for the vaccination object in jdbcconnection class
    //============================

public ArrayList<Vaccination> getVaccinationData(String country, String region, String antigen, String yearStart, String yearEnd) {
    ArrayList<Vaccination> results = new ArrayList<>();
    Connection connection = null;

    try {
        connection = DriverManager.getConnection(DATABASE);
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("c.name as country_name, ");
        query.append("r.region as region_name, ");
        query.append("a.name as antigen_name, ");
        query.append("v.year as year, ");
        query.append("v.coverage as coverage, ");
        query.append("v.target_num as target_num, ");
        query.append("v.doses as doses ");
        query.append("FROM Vaccination v ");
        query.append("JOIN Country c ON v.country = c.CountryID ");
        query.append("JOIN Region r ON c.region = r.RegionID ");
        query.append("JOIN Antigen a ON v.antigen = a.AntigenID ");
        query.append("WHERE 1=1 ");
        
        // Add filters based on provided parameters
        if (country != null && !country.isEmpty()) {
            query.append("AND c.name = '").append(country).append("' ");
        }
        if (region != null && !region.isEmpty()) {
            query.append("AND r.region = '").append(region).append("' ");
        }
        if (antigen != null && !antigen.isEmpty()) {
            query.append("AND a.name = '").append(antigen).append("' ");
        }
        if (yearStart != null && !yearStart.isEmpty()) {
            query.append("AND v.year >= ").append(yearStart).append(" ");
        }
        if (yearEnd != null && !yearEnd.isEmpty()) {
            query.append("AND v.year <= ").append(yearEnd).append(" ");
        }
        
        query.append("ORDER BY v.year");

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        ResultSet resultSet = statement.executeQuery(query.toString());

        while (resultSet.next()) {
            // Create Vaccination object using the constructor
            String countryName = resultSet.getString("country_name");
            String antigenName = resultSet.getString("antigen_name");
            int year = resultSet.getInt("year");
            double targetNum = resultSet.getDouble("target_num");
            double doses = resultSet.getDouble("doses");
            double coverage = resultSet.getDouble("coverage");
            
            // Note: infType is not available in the query, so we'll set it to null or empty
            Vaccination vaccination = new Vaccination("", antigenName, countryName, year, targetNum, doses, coverage);
            results.add(vaccination);
        }

        statement.close();
    } catch (SQLException e) {
        System.err.println(e.getMessage());
    } finally {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    return results;
}

// Keep the HashMap version as backup
    public ArrayList<HashMap<String, String>> getVaccinationDataMap(String country, String region, String antigen, String yearStart, String yearEnd) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("c.name as country_name, ");
        query.append("r.region as region_name, ");
        query.append("a.name as antigen_name, ");
        query.append("v.year as year, ");
        query.append("v.coverage as coverage, ");
        query.append("v.target_num as target_num, ");
        query.append("v.doses as doses ");
        query.append("FROM Vaccination v ");
        query.append("JOIN Country c ON v.country = c.CountryID ");
        query.append("JOIN Region r ON c.region = r.RegionID ");
        query.append("JOIN Antigen a ON v.antigen = a.AntigenID ");
        query.append("WHERE 1=1 ");
        
        if (country != null && !country.isEmpty()) {
            query.append("AND c.name = '").append(country).append("' ");
        }
        if (region != null && !region.isEmpty()) {
            query.append("AND r.region = '").append(region).append("' ");
        }
        if (antigen != null && !antigen.isEmpty()) {
            query.append("AND a.name = '").append(antigen).append("' ");
        }
        if (yearStart != null && !yearStart.isEmpty()) {
            query.append("AND v.year >= ").append(yearStart).append(" ");
        }
        if (yearEnd != null && !yearEnd.isEmpty()) {
            query.append("AND v.year <= ").append(yearEnd).append(" ");
        }
        
        query.append("ORDER BY v.year");
        
        return executeQuery(query.toString());
    }

//===========================
    // Add methods for the infection data object in jdbcconnection class
    //============================
    public ArrayList<InfectionData> getInfectionData(String infType, String country, String yearStart, String yearEnd) {
        ArrayList<InfectionData> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("it.description as inf_type, ");
            query.append("c.name as country_name, ");
            query.append("id.year as year, ");
            query.append("id.cases as cases ");
            query.append("FROM InfectionData id ");
            query.append("JOIN Infection_Type it ON id.inf_type = it.id ");
            query.append("JOIN Country c ON id.country = c.CountryID ");
            query.append("WHERE 1=1 ");
            
            if (infType != null && !infType.isEmpty()) {
                query.append("AND it.description = '").append(infType).append("' ");
            }
            if (country != null && !country.isEmpty()) {
                query.append("AND c.name = '").append(country).append("' ");
            }
            if (yearStart != null && !yearStart.isEmpty()) {
                query.append("AND id.year >= ").append(yearStart).append(" ");
            }
            if (yearEnd != null && !yearEnd.isEmpty()) {
                query.append("AND id.year <= ").append(yearEnd).append(" ");
            }
            
            query.append("ORDER BY id.year");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query.toString());

            while (resultSet.next()) {
                String infectionType = resultSet.getString("inf_type");
                String countryName = resultSet.getString("country_name");
                int year = resultSet.getInt("year");
                double cases = resultSet.getDouble("cases");
                
                InfectionData infectionData = new InfectionData(infectionType, countryName, year, cases);
                results.add(infectionData);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    //keep the HashMap version as backup
    public ArrayList<HashMap<String, String>> getInfectionDataMap(String infType, String country, String yearStart, String yearEnd) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("it.description as inf_type, ");
        query.append("c.name as country_name, ");
        query.append("id.year as year, ");
        query.append("id.cases as cases ");
        query.append("FROM InfectionData id ");
        query.append("JOIN Infection_Type it ON id.inf_type = it.id ");
        query.append("JOIN Country c ON id.country = c.CountryID ");
        query.append("WHERE 1=1 ");

        if (infType != null && !infType.isEmpty()) {
            query.append("AND it.description = '").append(infType).append("' ");
        }
        if (country != null && !country.isEmpty()) {
            query.append("AND c.name = '").append(country).append("' ");
        }
        if (yearStart != null && !yearStart.isEmpty()) {
            query.append("AND id.year >= ").append(yearStart).append(" ");
        }
        if (yearEnd != null && !yearEnd.isEmpty()) {
            query.append("AND id.year <= ").append(yearEnd).append(" ");
        }

        query.append("ORDER BY id.year");

        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query.toString());

            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                row.put("inf_type", resultSet.getString("inf_type"));
                row.put("country_name", resultSet.getString("country_name"));
                row.put("year", String.valueOf(resultSet.getInt("year")));
                row.put("cases", String.valueOf(resultSet.getDouble("cases")));
                results.add(row);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    
    // Method to get infection types for the dropdown
        public ArrayList<String> getInfectionTypes() {
            ArrayList<String> infectionTypes = new ArrayList<>();
            Connection connection = null;
            
            try {
                connection = DriverManager.getConnection(DATABASE);
                String query = "SELECT DISTINCT description FROM Infection_Type ORDER BY description";
                
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30);
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    infectionTypes.add(resultSet.getString("description"));
                }

                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
            
            return infectionTypes;
        }
}