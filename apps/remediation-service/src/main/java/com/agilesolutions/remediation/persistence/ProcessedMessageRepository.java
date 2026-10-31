package com.agilesolutions.remediation.persistence;

import com.agilesolutions.remediation.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}
