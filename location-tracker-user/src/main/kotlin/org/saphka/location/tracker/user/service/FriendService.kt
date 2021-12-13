package org.saphka.location.tracker.user.service

import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper
import org.saphka.location.tracker.user.dao.FriendDAO
import org.saphka.location.tracker.user.grpc.*
import org.saphka.location.tracker.user.model.Friend
import org.saphka.location.tracker.user.model.FriendStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FriendService {
    fun getFriends(userId: Int): Flux<Friend>
    fun confirmFriend(userId: Int, friendId: Int): Mono<Friend>
    fun createRequest(userId: Int, friendId: Int): Mono<Friend>
}

@Service
class FriendServiceImpl(private val friendDAO: FriendDAO) : FriendService {
    override fun getFriends(userId: Int): Flux<Friend> {
        return friendDAO.getFriends(userId)
    }

    override fun confirmFriend(userId: Int, friendId: Int): Mono<Friend> {
        return friendDAO.updateRequestStatus(userId, friendId, FriendStatus.CONFIRMED)
    }

    override fun createRequest(userId: Int, friendId: Int): Mono<Friend> {
        return friendDAO.createFriendRequest(userId, friendId)
    }
}

@GRpcService
class FriendGrpcService(
    private val friendService: FriendService,
    private val userService: UserService
) :
    FriendServiceGrpc.FriendServiceImplBase() {

    @Secured
    override fun getPendingFriendRequests(
        request: DummyMessage,
        responseObserver: StreamObserver<FriendConfirmationMultipleResponse>
    ) {
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapMany {
                    Flux.deferContextual {
                        val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            (authentication as JwtAuthenticationToken).token.subject.toInt()
                        friendService.getFriends(sub)
                    }
                }
                .filter {
                    it.status == FriendStatus.PENDING
                }
                .collectList()
                .map { mapToPendingResponse(it) }
        }
    }

    @Secured
    override fun confirmFriend(
        request: FriendConfirmation,
        responseObserver: StreamObserver<DummyMessage>
    ) {
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req.flatMap { confirmation ->
                Mono.deferContextual {
                    val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    friendService.confirmFriend(sub, confirmation.friendId)
                }
            }.map { DummyMessage.getDefaultInstance() }
        }
    }

    @Secured
    override fun addFriend(
        request: FriendRequest,
        responseObserver: StreamObserver<DummyMessage>
    ) {
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req.flatMap {
                userService.getUserByAlias(it.alias)
            }.zipWith(Mono.deferContextual {
                val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                val sub =
                    (authentication as JwtAuthenticationToken).token.subject.toInt()
                Mono.just(sub)
            }).flatMap {
                friendService.createRequest(friendId = it.t1.id, userId = it.t2)
            }.map { DummyMessage.getDefaultInstance() }
        }
    }

    private fun mapToPendingResponse(list: List<Friend>): FriendConfirmationMultipleResponse {
        return FriendConfirmationMultipleResponse.newBuilder().apply {
            list.forEach { friend ->
                this.addConfirmationBuilder().friendId = friend.friendId
            }
        }.build()
    }

}