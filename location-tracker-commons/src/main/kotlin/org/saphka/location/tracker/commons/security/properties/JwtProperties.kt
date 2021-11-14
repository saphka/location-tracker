package org.saphka.location.tracker.commons.security.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.core.io.Resource

@ConfigurationProperties("jwt")
@ConstructorBinding
data class JwtProperties(
    val issuer: String,
    val lifetime: Long,
    val keystore: Resource,
    val keystorePassword: String
)
