package com.example.weatherInfo.weatherInfo.entities;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class WeatherEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pincode;
    private LocalDate forDate;
    @Column(columnDefinition = "jsonb")
    private String weatherData;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPincode() {
		return pincode;
	}
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}
	public LocalDate getForDate() {
		return forDate;
	}
	public void setForDate(LocalDate forDate) {
		this.forDate = forDate;
	}
	public String getWeatherData() {
		return weatherData;
	}
	public void setWeatherData(String weatherData) {
		this.weatherData = weatherData;
	}
    
}
