package org.saphka.location.tracker.user.service

import com.google.protobuf.ByteString
import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.grpc.*
import org.saphka.location.tracker.user.model.User
import org.springframework.security.access.annotation.Secured
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
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
class UserServiceGrpcImpl(private val userService: UserService, private val jwtService: JwtService) :
    UserServiceGrpc.UserServiceImplBase() {

    @Secured
    override fun getUserInfo(request: DummyRequest, responseObserver: StreamObserver<UserResponse>) {
        return GrpcUtil.processCall(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap {
                    Mono.deferContextual {
                        val context = it.get<Context>(GrpcUtil.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            jwtService.parseToken((authentication as BearerTokenAuthenticationToken).token).body.subject.toInt()
                        userService.getUserById(sub)
                    }
                }
                .map {
                    mapToUserResponse(it)
                }
        }
    }

    @Secured
    override fun changeUser(request: UserChangeRequest, responseObserver: StreamObserver<UserResponse>) {
        return GrpcUtil.processCall(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap { userChangeData ->
                    Mono.deferContextual {
                        val context = it.get<Context>(GrpcUtil.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            jwtService.parseToken((authentication as BearerTokenAuthenticationToken).token).body.subject.toInt()
                        userService.updateUser(sub, userChangeData)
                    }
                }
                .map {
                    mapToUserResponse(it)
                }
        }
    }

    override fun authUser(request: UserAuthRequest, responseObserver: StreamObserver<TokenResponse>) {
        GrpcUtil.processCall(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap { userService.authUser(it) }
                .map {
                    TokenResponse.newBuilder()
                        .setToken(it)
                        .build()
                }
        }
    }

    override fun register(request: UserCreateRequest, responseObserver: StreamObserver<UserResponse>) {
        return GrpcUtil.processCall(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap { userService.createUser(it) }
                .map {
                    mapToUserResponse(it)
                }
        }
    }

    private fun mapToUserResponse(it: User) = UserResponse.newBuilder()
        .setId(it.id)
        .setAlias(it.alias)
        .setPublicKey(ByteString.copyFrom(it.publicKey))
        .build()

}