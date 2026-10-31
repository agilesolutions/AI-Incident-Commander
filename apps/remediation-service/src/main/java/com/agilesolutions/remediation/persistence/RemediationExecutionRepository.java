package com.agilesolutions.remediation.persistence;

import com.agilesolutions.remediation.domain.RemediationExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemediationExecutionRepository extends JpaRepository<RemediationExecution, String> {
    List<RemediationExecution> findTop100ByOrderByReceivedAtDesc();
}
