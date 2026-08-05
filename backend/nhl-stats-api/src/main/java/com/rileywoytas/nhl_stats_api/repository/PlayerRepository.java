package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByNhlId(Integer nhlId);

    @Query("SELECT p FROM Player p WHERE p.nhlId IN :nhlIds")
    List<Player> findByNhlIdIn(@Param("nhlIds") Set<Integer> nhlIds);
}
