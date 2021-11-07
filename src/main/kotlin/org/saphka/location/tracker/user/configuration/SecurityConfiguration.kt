package org.saphka.location.tracker.user.configuration

import org.saphka.location.tracker.user.configuration.properties.JwtProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey

private const val TOKEN_ALIAS = "token"
private const val BEARER = "Bearer "

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

}