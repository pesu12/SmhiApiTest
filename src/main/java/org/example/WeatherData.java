package org.example;

import java.time.LocalDateTime;

public record WeatherData(
        LocalDateTime time,
        double temperature,
        String quality,
        String info
) {}
