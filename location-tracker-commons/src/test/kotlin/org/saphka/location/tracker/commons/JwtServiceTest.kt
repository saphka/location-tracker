package org.saphka.location.tracker.commons

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitLessThanOffset
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.saphka.location.tracker.commons.security.JwtServiceImpl
import org.saphka.location.tracker.commons.security.properties.JwtProperties
import org.springframework.core.io.Resource
import java.net.URL
import java.security.KeyPairGenerator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class JwtServiceTest {

    private val keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
    private val service = JwtServiceImpl(
        JwtProperties(
            "http://www.issuer.com",
            15,
            Mockito.mock(Resource::class.java),
            "empty"
        ),
        keyPair
    )
    private val mapper = ObjectMapper()

    @Test
    fun `Token create test`() {
        val token = service.createToken("subj")

        val split = token.split(".")
        assertThat(split).hasSize(3)

        val headerString = Base64.getDecoder().decode(split[0])
        val bodyString = Base64.getDecoder().decode(split[1])

        val header: Map<String, Any> = mapper.readValue(
            headerString,
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
        )

        val body: Map<String, Any> = mapper.readValue(
            bodyString,
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
        )

        assertThat(header)
            .hasSize(2)
            .containsEntry("alg", "RS256")
            .containsEntry("typ", "JWT")

        assertThat(body)
            .hasSize(5)
            .containsEntry("iss", "http://www.issuer.com")
            .containsEntry("sub", "subj")
            .containsKey("iat")
            .containsKey("exp")
            .containsEntry("scope", listOf("user"))

    }

    @Test
    fun `Parse test`() {
        val token = service.createToken("subj")

        val jwt = service.decode(token)

        assertThat(jwt.issuer).isEqualTo(URL("http://www.issuer.com"))
        assertThat(jwt.subject).isEqualTo("subj")
        assertThat(jwt.issuedAt).isCloseTo(Instant.now(), TemporalUnitLessThanOffset(1, ChronoUnit.SECONDS))
        assertThat(jwt.expiresAt).isCloseTo(
            Instant.now().plus(15, ChronoUnit.MINUTES),
            TemporalUnitLessThanOffset(1, ChronoUnit.SECONDS)
        )
        assertThat(jwt.getClaimAsStringList("scope")).isEqualTo(listOf("user"))
    }

}