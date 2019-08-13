package com.dartsBot.dartsBot.repository;

import com.dartsBot.dartsBot.entities.Match;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends CrudRepository<Match, Long> {
}
