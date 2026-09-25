package com.rileywoytas.nhl_stats_api.draft.repository;

import com.rileywoytas.nhl_stats_api.draft.model.DraftSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DraftSessionRepository extends JpaRepository<DraftSession, Long> {

    List<DraftSession> findAllByOrderByCreatedAtDesc();
}
