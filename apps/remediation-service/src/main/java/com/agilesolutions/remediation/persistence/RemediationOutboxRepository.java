package com.agilesolutions.remediation.persistence;

import com.agilesolutions.remediation.domain.RemediationOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemediationOutboxRepository extends JpaRepository<RemediationOutboxEvent, String> {
    List<RemediationOutboxEvent> findTop30ByPublishedAtIsNullOrderByCreatedAtAsc();
}
