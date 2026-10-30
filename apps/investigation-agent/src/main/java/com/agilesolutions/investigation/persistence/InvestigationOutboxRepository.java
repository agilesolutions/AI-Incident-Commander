package com.agilesolutions.investigation.persistence;
import com.agilesolutions.investigation.domain.InvestigationOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InvestigationOutboxRepository extends JpaRepository<InvestigationOutboxEvent, String> {
    List<InvestigationOutboxEvent> findTop30ByPublishedAtIsNullOrderByCreatedAtAsc();
}
