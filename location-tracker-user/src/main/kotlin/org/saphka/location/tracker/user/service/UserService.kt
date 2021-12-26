package org.saphka.location.tracker.user.service

import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcCoroutineWrapper
import org.saphka.location.tracker.commons.security.JwtService
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.grpc.DummyMessage
import org.saphka.location.tracker.user.grpc.TokenResponse
import org.saphka.location.tracker.user.grpc.UserAuthRequest
import org.saphka.location.tracker.user.grpc.UserChangeRequest
import org.saphka.location.tracker.user.grpc.UserCreateRequest
import org.saphka.location.tracker.user.grpc.UserResponse
import org.saphka.location.tracker.user.grpc.UserServiceGrpc
import org.saphka.location.tracker.user.model.User
import org.springframework.security.access.annotation.Secured
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

interface UserService {
    fun getUserById(id: Int): Flow<User>
    fun getUserByAlias(alias: String): Flow<User>
    fun getUserFriends(id: Int): Flow<User>
    fun authUser(authRequest: UserAuthRequest): Flow<String>
    fun createUser(createRequest: UserCreateRequest): Flow<User>
    fun updateUser(id: Int, updateRequest: UserChangeRequest): Flow<User>
}

@Service
class UserServiceImpl(
    private val userDAO: UserDAO,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun getUserById(id: Int): Flow<User> {
        return userDAO.findUser(id)
    }

    override fun getUserByAlias(alias: String): Flow<User> {
        return userDAO.findUser(alias)
    }

    override fun getUserFriends(id: Int): Flow<User> {
        return userDAO.findUsersFriends(id)
    }

    override fun authUser(authRequest: UserAuthRequest): Flow<String> {
        return userDAO.findUser(authRequest.alias)
            .filter { passwordEncoder.matches(authRequest.password, it.passwordHash) }
            .onEmpty {
                Status.UNAUTHENTICATED.withDescription("Password does not match").asRuntimeException()
            }
            .map { jwtService.createToken(it.id.toString()) }
    }

    override fun createUser(createRequest: UserCreateRequest): Flow<User> {
        return userDAO.createUser(
            createRequest.alias,
            createRequest.publicKey.toByteArray(),
            passwordEncoder.encode(createRequest.password)
        )
    }

    override fun updateUser(id: Int, updateRequest: UserChangeRequest): Flow<User> {
        return userDAO.findUser(id)
            .map {
                User(
                    it.id,
                    it.alias,
                    updateRequest.publicKey.toByteArray(),
                    it.passwordHash
                )
            }
            .flatMapConcat { userDAO.updateUser(it) }
    }
}

@GRpcService
class UserServiceGrpcImpl(private val userService: UserService) :
    UserServiceGrpc.UserServiceImplBase() {

    @Secured
    override fun getCurrentUserInfo(request: DummyMessage, responseObserver: StreamObserver<UserResponse>) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat {
                    val context = GrpcCoroutineWrapper.contextHolder.get()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    userService.getUserById(sub)
                }
                .map {
                    mapToUserResponse(it)
                }
        }
    }

    @Secured
    override fun changeCurrentUser(request: UserChangeRequest, responseObserver: StreamObserver<UserResponse>) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat { userChangeData ->
                    val context = GrpcCoroutineWrapper.contextHolder.get()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    userService.updateUser(sub, userChangeData)
                }
                .map { mapToUserResponse(it) }
        }
    }

    override fun authUser(request: UserAuthRequest, responseObserver: StreamObserver<TokenResponse>) {
        GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat { userService.authUser(it) }
                .map {
                    TokenResponse.newBuilder()
                        .setToken(it)
                        .build()
                }
        }
    }

    override fun register(request: UserCreateRequest, responseObserver: StreamObserver<UserResponse>) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat { userService.createUser(it) }
                .map { mapToUserResponse(it) }
        }
    }

    @Secured
    override fun getCurrentUserFriends(
        request: DummyMessage,
        responseObserver: StreamObserver<UserResponse>
    ) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat {
                    val context = GrpcCoroutineWrapper.contextHolder.get()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    userService.getUserFriends(sub)
                }
                .map { mapToUserResponse(it) }
        }
    }

    private fun mapToUserResponse(user: User) = UserResponse.newBuilder().apply {
        id = user.id
        alias = user.alias
        publicKey = ByteString.copyFrom(user.publicKey)
    }.build()

}