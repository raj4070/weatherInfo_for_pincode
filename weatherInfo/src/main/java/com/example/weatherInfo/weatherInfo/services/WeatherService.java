package com.example.weatherInfo.weatherInfo.services;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

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
        Optional<WeatherEntity> optionalWeatherInfo = weatherInfoRepository.findByPincodeAndForDate(pincode, forDate);
        if (optionalWeatherInfo.isPresent()) {
            return optionalWeatherInfo.get();
        }

        PincodeEntity pincodeInfo = pincodeInfoRepository.findById(pincode).orElseGet(() -> {
        	PincodeEntity newInfo = getPincodeInfoFromApi(pincode);
            pincodeInfoRepository.save(newInfo);
            return newInfo;
        });

        WeatherEntity weatherInfo = getWeatherFromApi(pincode, pincodeInfo.getLatitude(), pincodeInfo.getLongitude(), forDate);
        weatherInfoRepository.save(weatherInfo);

        return weatherInfo;
    }

    private PincodeEntity getPincodeInfoFromApi(String pincode) {
    	
        String url = "https://api.openweathermap.org/geo/1.0/zip?zip=" + pincode + ",in&appid=" + apiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
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
    	
    	long unixTimestamp = forDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid="
                + apiKey + "&dt=" + unixTimestamp;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JSONObject weatherData = new JSONObject(response.getBody());

        WeatherEntity weatherInfo = new WeatherEntity();
        weatherInfo.setPincode(pincode);
        weatherInfo.setForDate(forDate);
        weatherInfo.setTemperature(weatherData.getJSONObject("main").getDouble("temp"));
        weatherInfo.setHumidity(weatherData.getJSONObject("main").getInt("humidity"));
        weatherInfo.setPressure(weatherData.getJSONObject("main").getInt("pressure"));
        weatherInfo.setWindSpeed(weatherData.getJSONObject("wind").getInt("speed"));
        weatherInfo.setDescription(weatherData.getJSONArray("weather").getJSONObject(0).getString("description"));
        weatherInfo.setPlace(weatherData.getString("name"));
        return weatherInfo;
    }
}

