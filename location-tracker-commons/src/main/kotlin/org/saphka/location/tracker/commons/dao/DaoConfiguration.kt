package org.saphka.location.tracker.commons.dao

import com.zaxxer.hikari.HikariDataSource
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(DataSourceProperties::class)
@PropertySource("classpath:dao.properties")
class DaoConfiguration {

    @Bean
    fun dslContext(connectionFactory: ConnectionFactory, sqlDialect: SQLDialect, jooqSettings: Settings): DSLContext {
        return DSL.using(connectionFactory, sqlDialect, jooqSettings)
    }

    @Bean
    fun jooqSettings() = Settings().apply {
        renderNameCase = RenderNameCase.LOWER_IF_UNQUOTED
        renderQuotedNames = RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED
    }

    @Bean
    fun sqlDialect() = SQLDialect.POSTGRES

    @Bean
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {
        return dataSourceProperties
            .initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }

}