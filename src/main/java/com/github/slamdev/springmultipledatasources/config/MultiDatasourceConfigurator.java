package com.github.slamdev.springmultipledatasources.config;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class MultiDatasourceConfigurator {

    private static final String ROOT_PROP = "smd";

    private MultiDatasourceConfigurator() {
        // Utility class
    }

    private static String overriddenProp(String propName, String qualifier) {
        propName = propName.replaceFirst("spring.", "");
        return ROOT_PROP + "." + qualifier + "." + propName;
    }

    public static HikariDataSource dataSource(Environment environment, String qualifier) {
        var binder = Binder.get(environment);

        var dataSourceProperties = binder
                .bind("spring.datasource", DataSourceProperties.class)
                .orElseGet(DataSourceProperties::new);
        dataSourceProperties = binder
                .bind(overriddenProp("spring.datasource", qualifier), Bindable.ofInstance(dataSourceProperties))
                .orElse(dataSourceProperties);

        var dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();

        dataSource = binder
                .bind("spring.datasource.hikari", Bindable.ofInstance(dataSource))
                .orElse(dataSource);
        dataSource = binder
                .bind(overriddenProp("spring.datasource.hikari", qualifier), Bindable.ofInstance(dataSource))
                .orElse(dataSource);

        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }

        return dataSource;
    }

    public static JdbcTemplate jdbcTemplate(DataSource dataSource, Environment environment, String qualifier) {
        var binder = Binder.get(environment);

        var properties = binder
                .bind("spring.jdbc", JdbcProperties.class)
                .orElseGet(JdbcProperties::new);
        properties = binder
                .bind(overriddenProp("spring.jdbc", qualifier), Bindable.ofInstance(properties))
                .orElse(properties);

        // Should be in sync with org.springframework.boot.autoconfigure.jdbc.JdbcTemplateConfiguration#jdbcTemplate()
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcProperties.Template template = properties.getTemplate();
        jdbcTemplate.setFetchSize(template.getFetchSize());
        jdbcTemplate.setMaxRows(template.getMaxRows());
        if (template.getQueryTimeout() != null) {
            jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
        }
        return jdbcTemplate;
    }

    public static NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public static Flyway flyway(DataSource dataSource, Environment environment, ResourceLoader resourceLoader, String qualifier) {
        var binder = Binder.get(environment);

        var properties = binder
                .bind("spring.flyway", FlywayProperties.class)
                .orElseGet(FlywayProperties::new);
        properties = binder
                .bind(overriddenProp("spring.flyway", qualifier), Bindable.ofInstance(properties))
                .orElse(properties);

        FluentConfiguration configuration = new FluentConfiguration(resourceLoader.getClassLoader());
        configuration.dataSource(dataSource);
        // Should be in sync with org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.FlywayConfiguration#configureProperties()
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        String[] locations = properties.getLocations().toArray(new String[0]);
        configuration.locations(locations);
        map.from(properties.isFailOnMissingLocations()).to(configuration::failOnMissingLocations);
        map.from(properties.getEncoding()).to(configuration::encoding);
        map.from(properties.getConnectRetries()).to(configuration::connectRetries);
        map.from(properties.getConnectRetriesInterval()).as(Duration::getSeconds).as(Long::intValue).to(configuration::connectRetriesInterval);
        map.from(properties.getLockRetryCount()).to(configuration::lockRetryCount);
        map.from(properties.getDefaultSchema()).to(configuration::defaultSchema);
        map.from(properties.getSchemas()).as(StringUtils::toStringArray).to(configuration::schemas);
        map.from(properties.isCreateSchemas()).to(configuration::createSchemas);
        map.from(properties.getTable()).to(configuration::table);
        map.from(properties.getTablespace()).to(configuration::tablespace);
        map.from(properties.getBaselineDescription()).to(configuration::baselineDescription);
        map.from(properties.getBaselineVersion()).to(configuration::baselineVersion);
        map.from(properties.getInstalledBy()).to(configuration::installedBy);
        map.from(properties.getPlaceholders()).to(configuration::placeholders);
        map.from(properties.getPlaceholderPrefix()).to(configuration::placeholderPrefix);
        map.from(properties.getPlaceholderSuffix()).to(configuration::placeholderSuffix);
        map.from(properties.getPlaceholderSeparator()).to(configuration::placeholderSeparator);
        map.from(properties.isPlaceholderReplacement()).to(configuration::placeholderReplacement);
        map.from(properties.getSqlMigrationPrefix()).to(configuration::sqlMigrationPrefix);
        map.from(properties.getSqlMigrationSuffixes()).as(StringUtils::toStringArray).to(configuration::sqlMigrationSuffixes);
        map.from(properties.getSqlMigrationSeparator()).to(configuration::sqlMigrationSeparator);
        map.from(properties.getRepeatableSqlMigrationPrefix()).to(configuration::repeatableSqlMigrationPrefix);
        map.from(properties.getTarget()).to(configuration::target);
        map.from(properties.isBaselineOnMigrate()).to(configuration::baselineOnMigrate);
        map.from(properties.isCleanDisabled()).to(configuration::cleanDisabled);
        map.from(properties.isCleanOnValidationError()).to(configuration::cleanOnValidationError);
        map.from(properties.isGroup()).to(configuration::group);
        map.from(properties.isMixed()).to(configuration::mixed);
        map.from(properties.isOutOfOrder()).to(configuration::outOfOrder);
        map.from(properties.isSkipDefaultCallbacks()).to(configuration::skipDefaultCallbacks);
        map.from(properties.isSkipDefaultResolvers()).to(configuration::skipDefaultResolvers);
        map.from(properties.isValidateMigrationNaming()).to(configuration::validateMigrationNaming);
        map.from(properties.isValidateOnMigrate()).to(configuration::validateOnMigrate);
        map.from(properties.getInitSqls()).whenNot(CollectionUtils::isEmpty).as((initSqls) -> StringUtils.collectionToDelimitedString(initSqls, "\n")).to(configuration::initSql);
        map.from(properties.getScriptPlaceholderPrefix()).to(configuration::scriptPlaceholderPrefix);
        map.from(properties.getScriptPlaceholderSuffix()).to(configuration::scriptPlaceholderSuffix);
        map.from(properties.isExecuteInTransaction()).to(configuration::executeInTransaction);
        map.from(properties::getLoggers).to(configuration::loggers);
        map.from(properties.getBatch()).to(configuration::batch);
        map.from(properties.getDryRunOutput()).to(configuration::dryRunOutput);
        map.from(properties.getErrorOverrides()).to(configuration::errorOverrides);
        map.from(properties.getLicenseKey()).to(configuration::licenseKey);
        map.from(properties.getStream()).to(configuration::stream);
        map.from(properties.getUndoSqlMigrationPrefix()).to(configuration::undoSqlMigrationPrefix);
        map.from(properties.getCherryPick()).to(configuration::cherryPick);
        map.from(properties.getJdbcProperties()).whenNot(Map::isEmpty).to(configuration::jdbcProperties);
        map.from(properties.getKerberosConfigFile()).to(configuration::kerberosConfigFile);
        map.from(properties.getOutputQueryResults()).to(configuration::outputQueryResults);
        map.from(properties.getSkipExecutingMigrations()).to(configuration::skipExecutingMigrations);
        map.from(properties.getIgnoreMigrationPatterns()).whenNot(List::isEmpty).to((ignoreMigrationPatterns) -> configuration.ignoreMigrationPatterns(ignoreMigrationPatterns.toArray(new String[0])));
        map.from(properties.getDetectEncoding()).to(configuration::detectEncoding);
        //
        return configuration.load();
    }

    public static FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }

    public static LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment environment, String[] packagesToScan, String qualifier) {
        var binder = Binder.get(environment);

        var properties = binder
                .bind("spring.jpa", JpaProperties.class)
                .orElseGet(JpaProperties::new);
        properties = binder
                .bind(overriddenProp("spring.jpa", qualifier), Bindable.ofInstance(properties))
                .orElse(properties);

        JpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(jpaVendorAdapter, properties.getProperties(), null);
        return builder.dataSource(dataSource)
                .packages(packagesToScan)
                .persistenceUnit(qualifier+"Ds")
                .build();
    }

    public static PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        return transactionManager;
    }
}
