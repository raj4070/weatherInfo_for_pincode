package com.example.weatherInfo.weatherInfo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.weatherInfo.weatherInfo.entities.PincodeEntity;

public interface  PincodeRepo extends JpaRepository<PincodeEntity, String>{

}
