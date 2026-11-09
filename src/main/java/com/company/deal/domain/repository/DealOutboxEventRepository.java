package com.company.deal.domain.repository;

import com.company.deal.domain.entity.DealOutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealOutboxEventRepository extends JpaRepository<DealOutboxEvent, String> {

    @Query("SELECT e FROM DealOutboxEvent e WHERE e.published = false ORDER BY e.createdAt ASC")
    List<DealOutboxEvent> findPendingEvents(Pageable pageable);
}
