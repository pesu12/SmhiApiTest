package org.example;

import org.json.JSONException;

import java.io.IOException;

public class SmhiGui {

    private static final String STATION = "Stockholm-Arlanda Flygplats";
    private static final String PERIOD = "latest-hour";
    private static final String TEMPERATURE_PARAMETER = "1";

    public void start() {
        SmhiApiClient client = new SmhiApiClient();
        try {
            String stationKey = client.getStationId(TEMPERATURE_PARAMETER, STATION);
            System.out.println("Station är: " + STATION);
            String periodName = client.getPeriodName(TEMPERATURE_PARAMETER, stationKey, PERIOD);
            //Parameter value 1 = Lufttemperatur;momentanvärde, 1 gång/tim;celsius

            WeatherData data = client.getData(TEMPERATURE_PARAMETER, stationKey, periodName);
            System.out.println("Tid: " + data.time());
            System.out.println("Temperatur: " + data.temperature());
            System.out.println("Kvalitet: " + data.quality());
            System.out.println("Info: " + data.info());

        } catch (IOException | JSONException e) {
            System.err.println("Fel vid SMHI-anrop: " + e.getMessage());
        }
    }
}
