package org.saphka.location.tracker.user.service

import com.google.protobuf.ByteString
import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper
import org.saphka.location.tracker.user.dao.FriendDAO
import org.saphka.location.tracker.user.grpc.DummyMessage
import org.saphka.location.tracker.user.grpc.FriendServiceGrpc
import org.saphka.location.tracker.user.grpc.UserMultipleResponse
import org.saphka.location.tracker.user.grpc.UserResponse
import org.saphka.location.tracker.user.model.Friend
import org.saphka.location.tracker.user.model.FriendStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FriendService {

    fun getFriends(userId: Int): Flux<Friend>

}

@Service
class FriendServiceImpl(private val friendDAO: FriendDAO) : FriendService {
    override fun getFriends(userId: Int): Flux<Friend> {
        return friendDAO.getFriends(userId)
    }
}

@GRpcService
class FriendGrpcService(
    private val friendService: FriendService,
    private val userService: UserService
) :
    FriendServiceGrpc.FriendServiceImplBase() {

    @Secured
    override fun getCurrentUserFriends(
        request: DummyMessage,
        responseObserver: StreamObserver<UserMultipleResponse>
    ) {
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap {
                    Mono.deferContextual {
                        val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            (authentication as JwtAuthenticationToken).token.subject.toInt()
                        Mono.just(sub)
                    }
                }
                .flatMap { userId ->
                    friendService.getFriends(userId)
                        .filter { friend -> friend.status == FriendStatus.CONFIRMED }
                        .collectList()
                        .map { list ->
                            mutableListOf<Int>().let { res ->
                                res.addAll(list
                                    .filter { friend -> friend.firstId != userId }
                                    .map { it.firstId }
                                )
                                res.addAll(list
                                    .filter { friend -> friend.secondId != userId }
                                    .map { it.secondId }
                                )
                                res
                            }
                        }
                }
                .flatMapMany { ids ->
                    userService.getUserByIds(ids)
                }
                .map {
                    UserResponse.newBuilder().let { builder ->
                        builder.id = it.id
                        builder.alias = it.alias
                        builder.publicKey = ByteString.copyFrom(it.publicKey)
                        builder.build()
                    }
                }
                .collectList()
                .map {
                    UserMultipleResponse.newBuilder()
                        .addAllUser(it)
                        .build()
                }
        }
    }

}