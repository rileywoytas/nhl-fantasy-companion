package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByNhlId(Long nhlId);
}