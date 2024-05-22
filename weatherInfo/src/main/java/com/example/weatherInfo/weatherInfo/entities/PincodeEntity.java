package com.example.weatherInfo.weatherInfo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PincodeEntity {
	@Id
    private String pincode;
    private Double latitude;
    private Double longitude;
	public String getPincode() {
		return pincode;
	}
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
    
}
