package com.github.slamdev.springmultipledatasources.pgsvc;

import com.github.slamdev.springmultipledatasources.config.PostgresConfiguration.PostgresQualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PGService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final IdentityRepository identityRepository;

    public PGService(@PostgresQualifier NamedParameterJdbcTemplate jdbcTemplate, IdentityRepository identityRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.identityRepository = identityRepository;
    }

    public List<Map<String, Object>> doSomething() {
        var entity = new IdentityEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(UUID.randomUUID().toString());
        identityRepository.save(entity);
        return jdbcTemplate.queryForList("SELECT * FROM identity", Map.of());
    }
}
