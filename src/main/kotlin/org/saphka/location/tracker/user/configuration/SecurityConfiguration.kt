package org.saphka.location.tracker.user.configuration

import org.saphka.location.tracker.user.configuration.properties.JwtProperties
import org.saphka.location.tracker.user.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey

private const val TOKEN_ALIAS = "token"

@Configuration
class SecurityConfiguration {

    @Bean
    fun jwtKeyPair(jwtProperties: JwtProperties): KeyPair {
        with(KeyStore.getInstance("JKS")) {
            jwtProperties.keystore.inputStream.use {
                load(it, jwtProperties.keystorePassword.toCharArray())
            }
            val private = getKey(TOKEN_ALIAS, jwtProperties.keystorePassword.toCharArray()) as PrivateKey
            val public = getCertificate(TOKEN_ALIAS).publicKey

            return KeyPair(public, private)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationManager: ReactiveAuthenticationManager,
        jwtAuthenticationConverter: ServerAuthenticationConverter,
        @Value("\${server.servlet.context-path:/api/v1/}") rootPath: String
    ): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)

        return http.authorizeExchange()
            .pathMatchers(HttpMethod.POST, "/users/register", "/users/auth")
//            .pathMatchers("/**")
            .permitAll()
            .pathMatchers("/**").hasRole("USER")
            .and()
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .httpBasic().disable()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .build()
    }

}

@Component
class JwtAuthenticationManager(private val jwtService: JwtService) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .map { jwtService.validateToken(it.credentials as String) }
            .onErrorResume { Mono.empty() }
            .map { token ->
                UsernamePasswordAuthenticationToken(
                    token.body.subject,
                    authentication.credentials as String,
                    token.body.get("roles", MutableList::class.java).map { SimpleGrantedAuthority(it as String) }
                )
            }
    }
}

@Component
class JwtServerAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> {
        return Mono.justOrEmpty(exchange)
            .flatMap { Mono.justOrEmpty(it.request.headers[AUTHORIZATION]) }
            .filter { it.isNotEmpty() }
            .map { it[0].substring("Bearer ".length) }
            .map { UsernamePasswordAuthenticationToken(it, it) }
    }
}