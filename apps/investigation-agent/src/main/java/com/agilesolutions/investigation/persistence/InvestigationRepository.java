package com.agilesolutions.investigation.persistence;
import com.agilesolutions.investigation.domain.InvestigationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvestigationRepository extends JpaRepository<InvestigationRecord, String> {}
