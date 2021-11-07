package org.saphka.location.tracker.user.service

import com.google.protobuf.ByteString
import io.grpc.CallOptions
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.grpc.*
import org.saphka.location.tracker.user.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {

    fun getUserById(id: Int): Mono<User>

    fun authUser(authRequest: UserAuthRequest): Mono<String>

    fun createUser(createRequest: UserCreateRequest): Mono<User>

    fun updateUser(id: Int, updateRequest: UserChangeRequest): Mono<User>
}

@Service
class UserServiceImpl(
    private val userDAO: UserDAO,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun getUserById(id: Int): Mono<User> {
        return userDAO.findUser(id)
    }

    override fun authUser(authRequest: UserAuthRequest): Mono<String> {
        return userDAO.findUser(authRequest.alias)
            .filter { passwordEncoder.matches(authRequest.password, it.passwordHash) }
            .switchIfEmpty(Mono.error { IllegalArgumentException("Username/password does not match") })
            .map { jwtService.createToken(it) }
    }

    override fun createUser(createRequest: UserCreateRequest): Mono<User> {
        return userDAO.createUser(
            createRequest.alias,
            createRequest.publicKey.toByteArray(),
            passwordEncoder.encode(createRequest.password)
        )
    }

    override fun updateUser(id: Int, updateRequest: UserChangeRequest): Mono<User> {
        return userDAO.findUser(id)
            .map {
                User(
                    it.id,
                    updateRequest.alias,
                    updateRequest.publicKey.toByteArray(),
                    it.passwordHash
                )
            }
            .flatMap { userDAO.updateUser(it) }
    }

}

@GRpcService
class UserServiceGrpcImpl(private val userService: UserService) : ReactorUserServiceGrpc.UserServiceImplBase() {

    override fun getUserInfo(request: Mono<DummyRequest>?): Mono<UserResponse> {
        return super.getUserInfo(request)
    }

    override fun changeUser(request: Mono<UserChangeRequest>?): Mono<UserResponse> {
        return super.changeUser(request)
    }

    override fun authUser(request: Mono<UserAuthRequest>?): Mono<TokenResponse> {
        return request!!
            .flatMap { userService.authUser(it) }
            .map {
                TokenResponse.newBuilder()
                    .setToken(it)
                    .build()
            }
    }

    override fun register(request: Mono<UserCreateRequest>?): Mono<UserResponse> {
        return request!!
            .flatMap { userService.createUser(it) }
            .map {
                mapToUserResponse(it)
            }
    }

    private fun mapToUserResponse(it: User) = UserResponse.newBuilder()
        .setId(it.id)
        .setAlias(it.alias)
        .setPublicKey(ByteString.copyFrom(it.publicKey))
        .build()

}