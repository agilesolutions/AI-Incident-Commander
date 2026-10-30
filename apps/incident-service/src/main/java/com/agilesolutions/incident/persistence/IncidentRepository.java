package com.agilesolutions.incident.persistence;

import com.agilesolutions.incident.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, String>, JpaSpecificationExecutor<Incident> {
    List<Incident> findTop100ByStatusOrderByDetectedAtDesc(IncidentStatus status);
}
