package com.example.weatherInfo.weatherInfo.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.weatherInfo.weatherInfo.entities.WeatherEntity;

public interface WeatherRepo extends JpaRepository<WeatherEntity,Long> {
	
	Optional<WeatherEntity> findByPincodeAndForDate(String pincode, LocalDate forDate);

}
