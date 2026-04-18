package com.defense.forensic.repository;

import com.defense.forensic.entity.AnalysisRequest;
import com.defense.forensic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
    List<AnalysisRequest> findByUserOrderByCreatedAtDesc(User user);
}
