package org.saphka.location.tracker.user.configuration

import com.zaxxer.hikari.HikariDataSource
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(DataSourceProperties::class)
class DaoConfiguration {

    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        return DSL.using(connectionFactory, SQLDialect.POSTGRES)
    }

    @Bean
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {
        return dataSourceProperties
            .initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }

}