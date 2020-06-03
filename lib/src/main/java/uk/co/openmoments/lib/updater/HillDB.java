package uk.co.openmoments.lib.updater;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class HillDB {

    public static String DATABASE_NAME = "hill_bagging.db";
    private static String RELATIVE_PATH = "app/src/main/assets/database/";
    private static String SQL_PATH = "lib/src/main/java/uk/co/openmoments/lib/updater/sql/";
    private String db_uri = "";
    private String sql_migration_path = "";
    private boolean database_exists;
    private int outputCount = 0;
    private enum HillCols {
        NUMBER, NAME, REGION, AREA, TOPO_SEL, COUNTY, CLASSIFICATION, METRES, FEET, HILL_BAG_URL, LATITUDE, LONGITUDE
    }
    EnumMap<HillCols, Integer> hillColMap;

    public HillDB() {
        try {
            String assetsPath = new File(RELATIVE_PATH).getCanonicalPath();
            database_exists = new File(assetsPath + "\\" + DATABASE_NAME).exists();
            db_uri = "jdbc:sqlite:" + assetsPath + "\\" + DATABASE_NAME;
            sql_migration_path = Paths.get(assetsPath, "db_migration.sql").toAbsolutePath().toString();

            hillColMap = new EnumMap<>(HillCols.class);
            hillColMap.put(HillCols.NUMBER, 0);
            hillColMap.put(HillCols.NAME, 1);
            hillColMap.put(HillCols.REGION, 5);
            hillColMap.put(HillCols.AREA, 6);
            hillColMap.put(HillCols.TOPO_SEL, 8);
            hillColMap.put(HillCols.COUNTY, 9);
            hillColMap.put(HillCols.CLASSIFICATION, 10);
            hillColMap.put(HillCols.METRES, 13);
            hillColMap.put(HillCols.FEET, 14);
            hillColMap.put(HillCols.HILL_BAG_URL, 30);
            hillColMap.put(HillCols.LATITUDE, 33);
            hillColMap.put(HillCols.LONGITUDE, 34);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void update(String hillCSV) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sql_migration_path))) {
            try (Connection conn = DriverManager.getConnection(db_uri)){
                if (conn != null) {
                    if (!database_exists) {
                        createDatabase(conn);
                    }
                    performUpdate(hillCSV, conn, writer);
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    private void createDatabase(Connection conn) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            String sqlPath = new File(SQL_PATH).getCanonicalPath();

            runSQLScript(sqlPath + "\\create_classification_table.sql", statement);
            runSQLScript(sqlPath + "\\create_hill_table.sql", statement);
            runSQLScript(sqlPath + "\\create_hill_classification.sql", statement);
            runSQLScript(sqlPath + "\\create_hill_walked_table.sql", statement);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void performUpdate(String hillsCSV, Connection conn, BufferedWriter writer) {
        System.out.println("Updating hill entries...");
        try (CSVReader reader = new CSVReader(new FileReader(hillsCSV))){
            reader.readAll().stream().skip(1).forEach(line -> {
                try {
                    processHill(line, conn, writer);
                } catch (SQLException | IOException e) {
                    System.err.println(e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void runSQLScript(String scriptPath, Statement statement) throws IOException {
        System.out.println("Running script: " + scriptPath);
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            reader.lines().forEach(line -> {
                try {
                    statement.execute(line);
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            });
        }
    }

    private void processHill(String[] line, Connection conn, BufferedWriter writer) throws SQLException, IOException {
        System.out.print(".");
        if (outputCount == 50) {
            System.out.println();
            outputCount = 0;
        }
        outputCount++;

        Statement statement = conn.createStatement();
        int hillNumber = Integer.parseInt(line[hillColMap.get(HillCols.NUMBER)]);
        float metres = Float.parseFloat(line[hillColMap.get(HillCols.METRES)]);
        float feet = Float.parseFloat(line[hillColMap.get(HillCols.FEET)]);
        float lat = Float.parseFloat(line[hillColMap.get(HillCols.LATITUDE)]);
        float lon = Float.parseFloat(line[hillColMap.get(HillCols.LONGITUDE)]);

        String sql = "SELECT name, region, area, topo_section, county, metres, feet, hill_url, latitude, longitude FROM hill WHERE number = " + hillNumber;
        ResultSet result = statement.executeQuery(sql);

        if (result.next()) {
            String name = result.getString("name");
            String region = result.getString("region");
            String area = result.getString("area");
            String topoSection = result.getString("topo_section");
            String county = result.getString("county");
            float dbMetres = result.getFloat("metres");
            float dbFeet = result.getFloat("feet");
            String hillUrl = result.getString("hill_url");
            float dbLat = result.getFloat("latitude");
            float dbLong = result.getFloat("longitude");

            StringBuilder updateBuilder = new StringBuilder();
            updateBuilder.append("UPDATE hill SET ");
            updateBuilder.append(colSqlFormat("name", name, line[hillColMap.get(HillCols.NAME)]));
            updateBuilder.append(colSqlFormat("region", region, line[hillColMap.get(HillCols.REGION)]));
            updateBuilder.append(colSqlFormat("area", area, line[hillColMap.get(HillCols.AREA)]));
            updateBuilder.append(colSqlFormat("topo_section", topoSection, line[hillColMap.get(HillCols.TOPO_SEL)]));
            updateBuilder.append(colSqlFormat("county", county, line[hillColMap.get(HillCols.COUNTY)]));
            updateBuilder.append(colSqlFormat("metres", dbMetres, metres));
            updateBuilder.append(colSqlFormat("feet", dbFeet, feet));
            updateBuilder.append(colSqlFormat("hill_url", hillUrl, line[hillColMap.get(HillCols.HILL_BAG_URL)]));
            updateBuilder.append(colSqlFormat("latitude", dbLat, lat));
            updateBuilder.append(colSqlFormat("longitude", dbLong, lon));

            if (updateBuilder.charAt(updateBuilder.length() -1) == ',') {
                updateBuilder.setLength(updateBuilder.length() - 1);
            }

            if (!updateBuilder.toString().equals("UPDATE hill SET ")) {
                updateBuilder.append(" WHERE number = ");
                updateBuilder.append(hillNumber);
                updateBuilder.append(";");

                System.out.println(updateBuilder.toString());
                statement.execute(updateBuilder.toString());
                updateBuilder.append("\r\n");
                writer.write(updateBuilder.toString());
            }
        } else {
            sql = "INSERT INTO hill(number, name, region, area, topo_section, county, metres, feet, hill_url, latitude, longitude) VALUES(" +
                    hillNumber + ", " +
                    "\"" + line[hillColMap.get(HillCols.NAME)] + "\", " +
                    "\"" + line[hillColMap.get(HillCols.REGION)] + "\", " +
                    "\"" + line[hillColMap.get(HillCols.AREA)] + "\", " +
                    "\"" + line[hillColMap.get(HillCols.TOPO_SEL)] + "\", " +
                    "\"" + line[hillColMap.get(HillCols.COUNTY)] + "\", " +
                    metres + ", " +
                    feet + ", " +
                    "\"" + line[hillColMap.get(HillCols.HILL_BAG_URL)] + "\", " +
                    lat + ", " +
                    lon + ");";
            statement.execute(sql);
            writer.write(sql + "\r\n");
        }


        // Process the classfications
        Map<String, Integer> knownClassifications = new HashMap<>();
        sql = "SELECT classification_id, classification FROM classification";
        result = statement.executeQuery(sql);
        while (result.next()) {
            knownClassifications.put(result.getString("classification"), result.getInt("classification_id"));
        }

        sql = "DELETE FROM hill_classification WHERE hill_id = " + hillNumber;
        statement.execute(sql);

        String[] classifications = line[hillColMap.get(HillCols.CLASSIFICATION)].trim().split(",");
        for (String classification : classifications) {
            if (classification.isEmpty() || knownClassifications.get(classification) == null) {
                continue;
            }
            sql = "INSERT INTO hill_classification (hill_id, classification_id) VALUES(" + hillNumber + ", " + knownClassifications.get(classification) + ");";
            statement.execute(sql);
        }
    }

    private String colSqlFormat(String column, String currentValue, String updateValue) {
        if (!currentValue.equals(updateValue)) {
            return new StringBuilder().append(column).append("=\"").append(updateValue).append("\",").toString();
        }
        return "";
    }

    private String colSqlFormat(String column, float currentValue, float updateValue) {
        if (Float.compare(currentValue, updateValue) != 0) {
            return new StringBuilder().append(column).append("=").append(updateValue).append(",").toString();
        }

        return "";
    }
}
