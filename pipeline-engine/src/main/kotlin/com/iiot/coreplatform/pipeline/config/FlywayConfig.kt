package com.iiot.coreplatform.pipeline.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig {

    @Bean
    fun postgresFlyway(
        @Value("\${spring.datasource.url}") url: String,
        @Value("\${spring.datasource.username}") username: String,
        @Value("\${spring.datasource.password}") password: String
    ): Flyway {
        return Flyway.configure()
            .dataSource(url, username, password)
            .locations("classpath:db/migration")
            .load()
            .also { it.migrate() }
    }

    @Bean
    fun timescaleFlyway(
        @Value("\${spring.timescale.datasource.url}") url: String,
        @Value("\${spring.timescale.datasource.username}") username: String,
        @Value("\${spring.timescale.datasource.password}") password: String
    ): Flyway {
        return Flyway.configure()
            .dataSource(
                url,
                username,
                password
            )
            .locations("classpath:db/migration-timescale")
            .mixed(true)
            .executeInTransaction(false)
            .load()
            .also { it.migrate() }
    }
}