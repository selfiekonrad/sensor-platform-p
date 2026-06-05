package com.ceniuch.sensorqueryservice.repository;

import com.ceniuch.db.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();
}