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
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        val jooqSettings = Settings()
        jooqSettings.renderNameCase = RenderNameCase.LOWER_IF_UNQUOTED
        jooqSettings.renderQuotedNames = RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED

        return DSL.using(connectionFactory, SQLDialect.POSTGRES, jooqSettings)
    }

    @Bean
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {
        return dataSourceProperties
            .initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }

}