package com.rileywoytas.nhl_stats_api.ranking.repository;

import com.rileywoytas.nhl_stats_api.ranking.model.PlayerRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRankingRepository extends JpaRepository<PlayerRanking, Long> {

    @Modifying
    @Query("delete from PlayerRanking r where r.source = :source")
    int deleteBySource(@Param("source") String source);

    List<PlayerRanking> findBySourceOrderBySourceRowAsc(String source);
}
