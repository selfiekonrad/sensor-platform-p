package com.ceniuch.sensordataprocessingservice.repository;

import com.ceniuch.db.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
}
