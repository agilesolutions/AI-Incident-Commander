package com.agilesolutions.investigation.persistence;
import com.agilesolutions.investigation.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {}
