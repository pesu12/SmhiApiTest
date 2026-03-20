package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;


/**
 * Example class for the SMHI metobs API. Uses org.json for JSON parsing.
 *
 */
public class SmhiApiClient {

    // Url for the metobs API
    private static final String MET_OBS_API  = "https://opendata-download-metobs.smhi.se/api";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String TEMPERATURE_HEADER =
            "Datum;Tid (UTC);Lufttemperatur;Kvalitet;;Tidsutsnitt:";
    /**
     * Print station name for the given parameter. Return the id for the displayed station.
     *
     * @param parameterKey The key for the wanted parameter
     * @param station The value for the station
     * @return The id for the last station
     * @throws IOException
     * @throws JSONException
     */
    public String getStationId(String parameterKey, String station) throws IOException, JSONException {

        JSONObject stationsObject = readJsonFromUrl(MET_OBS_API  + "/version/latest/parameter/" + parameterKey + ".json");
        JSONArray stationsArray = stationsObject.getJSONArray("station");

        for (int i = 0; i < stationsArray.length(); i++) {
            String tempStationName = stationsArray.getJSONObject(i).getString("name");
            if (tempStationName.equals(station)) {
                return stationsArray.getJSONObject(i).getString("key");
            }
        }
        throw new IllegalArgumentException("Kunde inte hitta station: " + station);
    }

    /**
     * Print all available periods for the given parameter and station. Return the key for the last period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey The key for the wanted station
     * @return The name for the last period
     * @throws IOException
     * @throws JSONException
     */
    public String getPeriodName(String parameterKey, String stationKey, String period) throws IOException, JSONException {

        JSONObject periodsObject = readJsonFromUrl(MET_OBS_API  + "/version/latest/parameter/" + parameterKey + "/station/" + stationKey + ".json");
        JSONArray periodsArray = periodsObject.getJSONArray("period");

        for (int i = 0; i < periodsArray.length(); i++) {
            String tempPeriodName = periodsArray.getJSONObject(i).getString("key");
            if (tempPeriodName.equals(period)) {
                return tempPeriodName;
            }
        }

        throw new IllegalArgumentException("Kunde inte hitta period: " + period);
    }


    /**
     * Get the data for the given parameter, station and period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey The key for the wanted station
     * @param periodName The name for the wanted period
     * @return The data
     * @throws IOException
     * @throws JSONException
     */
    public WeatherData getData(String parameterKey, String stationKey, String periodName) throws IOException {
        String returnString = readStringFromUrl(MET_OBS_API  + "/version/latest/parameter/" + parameterKey + "/station/" + stationKey + "/period/" + periodName + "/data.csv");
        String row = extractFirstDataRow(returnString).split("\\R")[1];
        return parseRow(row);
    }

    private WeatherData parseRow(String line) {

        String[] parts = line.split(";");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Ogiltig CSV-rad: " + line);
        }

        String date = parts[0];
        String time = parts[1];
        double temperature = Double.parseDouble(parts[2]);
        String quality = parts[3];
        String info = parts.length > 5 ? parts[5] : "";

        LocalDateTime dateTime = LocalDateTime.parse(date + "T" + time);

        return new WeatherData(dateTime, temperature, quality, info);
    }

    private String extractFirstDataRow(String returnString) {

        StringBuilder sb = new StringBuilder();

        boolean foundHeader = false;

        for (String line : returnString.split("\\R")) {

            if (line.trim().equals(TEMPERATURE_HEADER)) {
                sb.append(line).append("\n");
                foundHeader = true;
                continue;
            }

            if (foundHeader && !line.trim().isEmpty()) {
                sb.append(line);
                break; // bara första raden efter header
            }
        }


        return sb.toString();
    }

    private JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        String text = readStringFromUrl(url);
        return new JSONObject(text);
    }


    public String readStringFromUrl(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
