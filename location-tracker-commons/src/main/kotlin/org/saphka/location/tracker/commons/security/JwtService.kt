package org.saphka.location.tracker.commons.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.saphka.location.tracker.commons.security.properties.JwtProperties
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

interface JwtService {
    fun createToken(subject: String): String

    fun parseToken(token: String): Jws<Claims>
}

@Service
class JwtServiceImpl(private val jwtProperties: JwtProperties, private val jwtKeyPair: KeyPair) : JwtService,
    JwtDecoder {
    override fun createToken(subject: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(subject)
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

    override fun parseToken(token: String): Jws<Claims> {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKeyPair.public)
                .build()
                .parseClaimsJws(token)
        } catch (e: RuntimeException) {
            throw JwtException("Bad token", e)
        }
    }

    override fun decode(token: String): Jwt {
        val claimsJws = parseToken(token)

        return Jwt(
            token,
            claimsJws.body.issuedAt.toInstant(),
            claimsJws.body.expiration.toInstant(),
            claimsJws.header,
            claimsJws.body
        )
    }

}
