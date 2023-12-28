package com.github.slamdev.springmultipledatasources.crdbsvc;

import com.github.slamdev.springmultipledatasources.config.CockroachConfiguration.CockroachQualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CRDBService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final TeamRepository teamRepository;

    public CRDBService(@CockroachQualifier NamedParameterJdbcTemplate jdbcTemplate, TeamRepository teamRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.teamRepository = teamRepository;
    }

    public List<Map<String, Object>> doSomething() {
        var entity = new TeamEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(UUID.randomUUID().toString());
        teamRepository.save(entity);
        return jdbcTemplate.queryForList("SELECT * FROM team", Map.of());
    }
}
