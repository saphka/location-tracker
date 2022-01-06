package org.saphka.location.tracker.user.service

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcCoroutineWrapper
import org.saphka.location.tracker.user.dao.FriendDAO
import org.saphka.location.tracker.user.grpc.DummyMessage
import org.saphka.location.tracker.user.grpc.FriendConfirmation
import org.saphka.location.tracker.user.grpc.FriendRequest
import org.saphka.location.tracker.user.grpc.FriendServiceGrpc
import org.saphka.location.tracker.user.model.Friend
import org.saphka.location.tracker.user.model.FriendStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

interface FriendService {
    fun getFriends(userId: Int): Flow<Friend>
    fun confirmFriend(userId: Int, friendId: Int): Flow<Friend>
    fun createRequest(userId: Int, friendId: Int): Flow<Friend>
}

@Service
class FriendServiceImpl(private val friendDAO: FriendDAO) : FriendService {
    override fun getFriends(userId: Int): Flow<Friend> {
        return friendDAO.getFriends(userId)
    }

    override fun confirmFriend(userId: Int, friendId: Int): Flow<Friend> {
        return friendDAO.updateRequestStatus(userId, friendId, FriendStatus.CONFIRMED)
    }

    override fun createRequest(userId: Int, friendId: Int): Flow<Friend> {
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
        responseObserver: StreamObserver<FriendConfirmation>
    ) {
        GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat {
                    val context = GrpcCoroutineWrapper.getContext()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    friendService.getFriends(sub)
                }
                .filter {
                    it.status == FriendStatus.PENDING
                }
                .map {
                    FriendConfirmation.newBuilder().apply {
                        friendId = it.friendId
                    }.build()
                }
        }
    }

    @Secured
    override fun confirmFriend(
        request: FriendConfirmation,
        responseObserver: StreamObserver<DummyMessage>
    ) {
        GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req.flatMapConcat { confirmation ->
                val context = GrpcCoroutineWrapper.getContext()
                val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                val sub =
                    (authentication as JwtAuthenticationToken).token.subject.toInt()
                friendService.confirmFriend(sub, confirmation.friendId)

            }.map { DummyMessage.getDefaultInstance() }
        }
    }

    @Secured
    override fun addFriend(
        request: FriendRequest,
        responseObserver: StreamObserver<DummyMessage>
    ) {
         GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req.flatMapConcat {
                userService.getUserByAlias(it.alias)
            }.flatMapConcat {
                val context = GrpcCoroutineWrapper.getContext()
                val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                val sub =
                    (authentication as JwtAuthenticationToken).token.subject.toInt()
                flowOf(Pair(it, sub))
            }.flatMapConcat {
                friendService.createRequest(friendId = it.first.id, userId = it.second)
            }.map { DummyMessage.getDefaultInstance() }
        }
    }

}