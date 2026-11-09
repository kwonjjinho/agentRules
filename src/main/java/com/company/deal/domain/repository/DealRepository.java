package com.company.deal.domain.repository;

import com.company.deal.domain.entity.Deal;
import com.company.deal.domain.model.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {

    Page<Deal> findByStatus(DealStatus status, Pageable pageable);

    @Query("SELECT d FROM Deal d WHERE d.status IN ('OPEN', 'ACHIEVED') AND d.endsAt > :now ORDER BY d.endsAt ASC")
    List<Deal> findActiveDeals(@Param("now") Instant now);

    @Query("SELECT d FROM Deal d WHERE d.status IN ('OPEN', 'ACHIEVED') AND d.endsAt <= :now")
    List<Deal> findExpiredActiveDeals(@Param("now") Instant now);
}
