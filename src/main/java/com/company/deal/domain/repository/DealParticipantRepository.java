package com.company.deal.domain.repository;

import com.company.deal.domain.entity.DealParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealParticipantRepository extends JpaRepository<DealParticipant, String> {

    List<DealParticipant> findByDealId(String dealId);

    List<DealParticipant> findByUserId(String userId);

    boolean existsByDealIdAndUserId(String dealId, String userId);
}
