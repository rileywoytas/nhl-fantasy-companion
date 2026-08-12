package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByNhlId(Long nhlId);

    @Query("SELECT g FROM Game g WHERE g.nhlId IN :nhlIds")
    List<Game> findByNhlIdIn(@Param("nhlIds") Set<Long> nhlIds);

    List<Game> findAllBySeason(String season);

    @Query("SELECT DISTINCT g.season FROM Game g ORDER BY g.season DESC")
    List<String> findDistinctSeasons();

    @Query("SELECT g.nhlId FROM Game g WHERE g.season = :season")
    List<Long> getAllNhlIdsBySeason(@Param("season") String season);

    @Query("SELECT g.nhlId FROM Game g WHERE g.season = :season AND g.gameState <> 'FUT'")
    List<Long> getNonFutureNhlIdsBySeason(@Param("season") String season);
}