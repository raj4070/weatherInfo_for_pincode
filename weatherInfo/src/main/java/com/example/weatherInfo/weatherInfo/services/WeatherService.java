package com.example.weatherInfo.weatherInfo.services;

import java.time.LocalDate;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.weatherInfo.weatherInfo.entities.PincodeEntity;
import com.example.weatherInfo.weatherInfo.entities.WeatherEntity;
import com.example.weatherInfo.weatherInfo.repositories.PincodeRepo;
import com.example.weatherInfo.weatherInfo.repositories.WeatherRepo;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final PincodeRepo pincodeInfoRepository;
    private final WeatherRepo weatherInfoRepository;
    private final RestTemplate restTemplate;

    public WeatherService(PincodeRepo pincodeInfoRepository, WeatherRepo weatherInfoRepository) {
        this.pincodeInfoRepository = pincodeInfoRepository;
        this.weatherInfoRepository = weatherInfoRepository;
        this.restTemplate = new RestTemplate();
    }

    public WeatherEntity getWeather(String pincode, LocalDate forDate) {
        // Check if weather info already exists in DB
        Optional<WeatherEntity> optionalWeatherInfo = weatherInfoRepository.findByPincodeAndForDate(pincode, forDate);
        if (optionalWeatherInfo.isPresent()) {
            return optionalWeatherInfo.get();
        }

        // Get lat/long for the pincode
        PincodeEntity pincodeInfo = pincodeInfoRepository.findById(pincode).orElseGet(() -> {
        	PincodeEntity newInfo = getPincodeInfoFromApi(pincode);
            pincodeInfoRepository.save(newInfo);
            return newInfo;
        });

        // Get weather data
        WeatherEntity weatherInfo = getWeatherFromApi(pincode, pincodeInfo.getLatitude(), pincodeInfo.getLongitude(), forDate);
        weatherInfoRepository.save(weatherInfo);

        return weatherInfo;
    }

    private PincodeEntity getPincodeInfoFromApi(String pincode) {
        // Use geocoding API to get lat/long for the pincode
        String url = String.format("https://api.openweathermap.org/geo/1.0/zip?zip=%s,IN&appid=%s", pincode, apiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        // Parse response to get latitude and longitude
        JSONObject jsonObject = new JSONObject(response.getBody());
        Double latitude = jsonObject.getDouble("lat");
        Double longitude = jsonObject.getDouble("lon");

        PincodeEntity pincodeInfo = new PincodeEntity();
        pincodeInfo.setPincode(pincode);
        pincodeInfo.setLatitude(latitude);
        pincodeInfo.setLongitude(longitude);
        return pincodeInfo;
    }

    private WeatherEntity getWeatherFromApi(String pincode, Double latitude, Double longitude, LocalDate forDate) {
        // Use OpenWeather API to get weather data
        String url = String.format("https://api.openweathermap.org/data/2.5/onecall?lat=%f&lon=%f&exclude=hourly,minutely,current,alerts&appid=%s", latitude, longitude, apiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        // Parse response to get weather data for the specific date
        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray dailyArray = jsonObject.getJSONArray("daily");
        String weatherData = null;
        for (int i = 0; i < dailyArray.length(); i++) {
            JSONObject daily = dailyArray.getJSONObject(i);
            LocalDate date = LocalDate.ofEpochDay(daily.getLong("dt")/86400);
            if (date.equals(forDate)) {
                weatherData = daily.toString();
                break;
            }
        }

        WeatherEntity weatherInfo = new WeatherEntity();
        weatherInfo.setPincode(pincode);
        weatherInfo.setForDate(forDate);
        weatherInfo.setWeatherData(weatherData);
        return weatherInfo;
    }
}

