package org.saphka.location.tracker.user.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.saphka.location.tracker.user.configuration.properties.JwtProperties
import org.saphka.location.tracker.user.model.User
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

interface JwtService {
    fun createToken(user: User): String

    fun validateToken(token: String): Jws<Claims>
}

@Service
class JwtServiceImpl(private val jwtProperties: JwtProperties, private val jwtKeyPair: KeyPair) : JwtService {
    override fun createToken(user: User): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(user.id.toString())
            .setIssuer(jwtProperties.issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(jwtProperties.lifetime, ChronoUnit.MINUTES)))
            .addClaims(
                mapOf(
                    "roles" to listOf("USER")
                )
            )
            .signWith(jwtKeyPair.private)
            .compact()
    }

    override fun validateToken(token: String): Jws<Claims> =
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKeyPair.public)
                .build()
                .parseClaimsJws(token)
        } catch (e: RuntimeException) {
            throw IllegalArgumentException("Bad token", e)
        }

}
