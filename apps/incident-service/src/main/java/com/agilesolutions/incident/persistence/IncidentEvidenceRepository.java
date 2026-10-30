package com.agilesolutions.incident.persistence;

import com.agilesolutions.incident.domain.IncidentEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentEvidenceRepository extends JpaRepository<IncidentEvidence, Long> {
    List<IncidentEvidence> findByIncidentIdOrderByRecordedAtAsc(String incidentId);
    boolean existsByIncidentIdAndSourceEventId(String incidentId, String sourceEventId);
}
