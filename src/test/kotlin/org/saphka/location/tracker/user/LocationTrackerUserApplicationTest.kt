package org.saphka.location.tracker.user

import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.lognet.springboot.grpc.autoconfigure.GRpcServerProperties
import org.saphka.location.tracker.user.grpc.DummyRequest
import org.saphka.location.tracker.user.grpc.UserChangeRequest
import org.saphka.location.tracker.user.grpc.UserCreateRequest
import org.saphka.location.tracker.user.grpc.UserServiceGrpc
import org.springframework.beans.factory.annotation.Autowired
import java.security.KeyPairGenerator

class LocationTrackerUserApplicationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var grpcProperties: GRpcServerProperties

    lateinit var channel: ManagedChannel
    lateinit var client: UserServiceGrpc.UserServiceBlockingStub

    @BeforeAll
    fun init() {
        channel = ManagedChannelBuilder
            .forTarget("dns:///localhost:${grpcProperties.runningPort}/")
            .usePlaintext()
            .build()
        client = UserServiceGrpc.newBlockingStub(channel)
    }

    @AfterAll
    fun cleanup() {
        channel.shutdown()
    }

    @Test
    fun `Register user`() {
        val encodedPublicKey = KeyPairGenerator.getInstance("RSA").genKeyPair().public.encoded
        val userResponse = client.register(
            UserCreateRequest.newBuilder()
                .setAlias("test")
                .setPassword("test")
                .setPublicKey(
                    ByteString.copyFrom(
                        encodedPublicKey
                    )
                )
                .build()
        )

        assertThat(userResponse.alias).isEqualTo("test")
        assertThat(userResponse.id).isEqualTo(1)
        assertThat(userResponse.publicKey.toByteArray()).isEqualTo(encodedPublicKey)
    }

    @Test
    fun `Get Me No Auth`() {
        val statusException =
            assertThrows<StatusRuntimeException> { client.getUserInfo(DummyRequest.getDefaultInstance()) }
        assertThat(statusException.status.code).isEqualTo(Status.UNAUTHENTICATED.code)
    }

    @Test
    fun `Change Me No Auth`() {
        val statusException =
            assertThrows<StatusRuntimeException> { client.changeUser(UserChangeRequest.getDefaultInstance()) }
        assertThat(statusException.status.code).isEqualTo(Status.UNAUTHENTICATED.code)
    }

}