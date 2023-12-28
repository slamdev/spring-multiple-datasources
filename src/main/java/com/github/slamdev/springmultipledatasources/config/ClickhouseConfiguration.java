package com.github.slamdev.springmultipledatasources.config;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnableJpaRepositories(
        basePackages = ClickhouseConfiguration.PACKAGE_TO_SCAN,
        entityManagerFactoryRef = ClickhouseConfiguration.QUALIFIER + "EntityManagerFactory",
        transactionManagerRef = ClickhouseConfiguration.QUALIFIER + "TransactionManager"
)
@Configuration(proxyBeanMethods = false)
public class ClickhouseConfiguration {

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface ClickhouseQualifier {
    }

    public static final String QUALIFIER = "clickhouse";

    public static final String PACKAGE_TO_SCAN = "com.github.slamdev.springmultipledatasources.chsvc";

    @Bean(name = QUALIFIER + "DataSource")
    @ClickhouseQualifier
    public HikariDataSource dataSource(Environment environment) {
        return MultiDatasourceConfigurator.dataSource(environment, QUALIFIER);
    }

    @Bean(name = QUALIFIER + "JdbcTemplate")
    @ClickhouseQualifier
    public JdbcTemplate jdbcTemplate(@ClickhouseQualifier DataSource dataSource, Environment environment) {
        return MultiDatasourceConfigurator.jdbcTemplate(dataSource, environment, QUALIFIER);
    }

    @Bean(name = QUALIFIER + "NamedParameterJdbcTemplate")
    @ClickhouseQualifier
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@ClickhouseQualifier JdbcTemplate jdbcTemplate) {
        return MultiDatasourceConfigurator.namedParameterJdbcTemplate(jdbcTemplate);
    }

    @Bean(name = QUALIFIER + "Flyway")
    @ClickhouseQualifier
    public Flyway flyway(@ClickhouseQualifier DataSource dataSource, Environment environment, ResourceLoader resourceLoader) {
        return MultiDatasourceConfigurator.flyway(dataSource, environment, resourceLoader, QUALIFIER);
    }

    @Bean(name = QUALIFIER + "FlywayInitializer")
    @ClickhouseQualifier
    public FlywayMigrationInitializer flywayInitializer(@ClickhouseQualifier Flyway flyway) {
        return MultiDatasourceConfigurator.flywayInitializer(flyway);
    }

    @Bean(name = QUALIFIER + "EntityManagerFactory")
    @ClickhouseQualifier
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@ClickhouseQualifier DataSource dataSource, Environment environment) {
        return MultiDatasourceConfigurator.entityManagerFactory(dataSource, environment, new String[]{PACKAGE_TO_SCAN}, QUALIFIER);
    }

    @Bean(name = QUALIFIER + "TransactionManager")
    @ClickhouseQualifier
    public PlatformTransactionManager transactionManager(@ClickhouseQualifier LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return MultiDatasourceConfigurator.transactionManager(entityManagerFactoryBean);
    }
}
