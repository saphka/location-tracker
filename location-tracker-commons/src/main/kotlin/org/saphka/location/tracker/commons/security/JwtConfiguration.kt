package org.saphka.location.tracker.commons.security

import org.saphka.location.tracker.commons.security.properties.JwtProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey

private const val TOKEN_ALIAS = "token"

@Configuration
@PropertySource("classpath:jwt.properties")
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfiguration {

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

}