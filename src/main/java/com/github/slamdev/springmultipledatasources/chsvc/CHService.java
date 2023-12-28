package com.github.slamdev.springmultipledatasources.chsvc;

import com.github.slamdev.springmultipledatasources.config.ClickhouseConfiguration.ClickhouseQualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CHService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ProductRepository productRepository;

    public CHService(@ClickhouseQualifier NamedParameterJdbcTemplate jdbcTemplate, ProductRepository productRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.productRepository = productRepository;
    }

    public List<Map<String, Object>> doSomething() {
        var entity = new ProductEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(UUID.randomUUID().toString());
        productRepository.save(entity);
        return jdbcTemplate.queryForList("SELECT * FROM product", Map.of());
    }
}
