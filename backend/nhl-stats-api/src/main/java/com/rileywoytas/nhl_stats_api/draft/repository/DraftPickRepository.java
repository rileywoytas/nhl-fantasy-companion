package com.rileywoytas.nhl_stats_api.draft.repository;

import com.rileywoytas.nhl_stats_api.draft.model.DraftPick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface DraftPickRepository extends JpaRepository<DraftPick, Long> {

    @Query("select p.player.id from DraftPick p where p.session.id = :sessionId")
    Set<UUID> findDraftedPlayerIds(@Param("sessionId") Long sessionId);
}
