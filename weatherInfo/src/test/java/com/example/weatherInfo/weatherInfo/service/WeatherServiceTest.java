package com.example.weatherInfo.weatherInfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.weatherInfo.weatherInfo.entities.PincodeEntity;
import com.example.weatherInfo.weatherInfo.entities.WeatherEntity;
import com.example.weatherInfo.weatherInfo.repositories.PincodeRepo;
import com.example.weatherInfo.weatherInfo.repositories.WeatherRepo;
import com.example.weatherInfo.weatherInfo.services.WeatherService;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private PincodeRepo pincodeRepo;

    @Mock
    private WeatherRepo weatherRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;


    @Test
    public void testGetWeatherFromRepository() {
        String pincode = "411014";
        LocalDate forDate = LocalDate.of(2020, 10, 15);
        WeatherEntity mockWeatherEntity = new WeatherEntity();
        mockWeatherEntity.setPincode(pincode);
        mockWeatherEntity.setForDate(forDate);
        
        Mockito.when(weatherRepo.findByPincodeAndForDate(pincode, forDate)).thenReturn(Optional.of(mockWeatherEntity));
        
        WeatherEntity weatherEntity = weatherService.getWeather(pincode, forDate);
        
        Mockito.verify(weatherRepo, times(1)).findByPincodeAndForDate(pincode, forDate);
        assertEquals(mockWeatherEntity, weatherEntity);
    }

    @Test
    public void testGetWeatherFromApi() {
        String apiKey = "test-api-key"; 

        String pincode = "411014";
        LocalDate forDate = LocalDate.of(2020, 10, 15);
        
        Mockito.when(weatherRepo.findByPincodeAndForDate(pincode, forDate)).thenReturn(Optional.empty());

        PincodeEntity mockPincodeEntity = new PincodeEntity();
        mockPincodeEntity.setPincode(pincode);
        mockPincodeEntity.setLatitude(10.0);
        mockPincodeEntity.setLongitude(20.0);
        
        Mockito.when(pincodeRepo.findById(pincode)).thenReturn(Optional.of(mockPincodeEntity));
        
        JSONObject mockWeatherResponse = new JSONObject();
        mockWeatherResponse.put("main", new JSONObject().put("temp", 25.0).put("humidity", 80).put("pressure", 1013));
        mockWeatherResponse.put("wind", new JSONObject().put("speed", 5));
        mockWeatherResponse.put("weather", new JSONArray().put(new JSONObject().put("description", "clear sky")));
        mockWeatherResponse.put("name", "Test City");
        
        ResponseEntity<String> mockWeatherResponseEntity = mock(ResponseEntity.class);
        Mockito.when(mockWeatherResponseEntity.getBody()).thenReturn(mockWeatherResponse.toString());
        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), eq(String.class))).thenReturn(mockWeatherResponseEntity);
        
        WeatherEntity weatherEntity = weatherService.getWeather(pincode, forDate);
        
        Mockito.verify(weatherRepo, times(1)).findByPincodeAndForDate(pincode, forDate);
        Mockito.verify(pincodeRepo, times(1)).findById(pincode);
        Mockito.verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
        Mockito.verify(weatherRepo, times(1)).save(any(WeatherEntity.class));
        
        assertEquals(pincode, weatherEntity.getPincode());
        assertEquals(forDate, weatherEntity.getForDate());
        assertEquals(25.0, weatherEntity.getTemperature());
        assertEquals(80, weatherEntity.getHumidity());
        assertEquals(1013, weatherEntity.getPressure());
        assertEquals(5, weatherEntity.getWindSpeed());
        assertEquals("clear sky", weatherEntity.getDescription());
        assertEquals("Test City", weatherEntity.getPlace());
    }
}
