package org.saphka.location.tracker.location.service

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcCoroutineWrapper
import org.saphka.location.tracker.location.dao.LocationDAO
import org.saphka.location.tracker.location.grpc.DummyMessage
import org.saphka.location.tracker.location.grpc.LocationMessage
import org.saphka.location.tracker.location.grpc.LocationMultiRequest
import org.saphka.location.tracker.location.grpc.LocationServiceGrpc
import org.saphka.location.tracker.location.grpc.PageRequest
import org.saphka.location.tracker.location.model.Location
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.time.Instant

interface LocationService {
    fun addLocation(userId: Int, request: LocationMultiRequest): Flow<Int>
    fun getLocations(userId: Int, request: PageRequest): Flow<Location>
}

private const val DEFAULT_PAGE_SIZE = 100

@Service
class LocationServiceImpl(private val locationDAO: LocationDAO) : LocationService {
    override fun addLocation(userId: Int, request: LocationMultiRequest): Flow<Int> {
        return flowOf(request.locationList)
            .map { list ->
                list.map {
                    Location(
                        Int.MAX_VALUE, // dummy value
                        it.targetFriendId,
                        userId,
                        Instant.ofEpochMilli(it.timestamp),
                        it.latitude.toByteArray(),
                        it.longitude.toByteArray()
                    )
                }
            }
            .flatMapConcat { locationDAO.addLocations(it) }
    }

    override fun getLocations(userId: Int, request: PageRequest): Flow<Location> {
        val page = if (request.hasPage()) request.page else 0
        val size = if (request.hasSize()) request.size else DEFAULT_PAGE_SIZE
        return locationDAO.getLocations(userId, page, size)
    }
}

@GRpcService
class LocationServiceGrpcImpl(private val locationService: LocationService) :
    LocationServiceGrpc.LocationServiceImplBase() {

    @Secured
    override fun addLocation(
        request: LocationMultiRequest,
        responseObserver: StreamObserver<DummyMessage>
    ) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat { addRequest ->
                    val context = GrpcCoroutineWrapper.contextHolder.get()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()
                    locationService.addLocation(sub, addRequest)
                }
                .map { DummyMessage.getDefaultInstance() }
        }
    }

    @Secured
    override fun getCurrentUserFriendsLocations(
        request: PageRequest,
        responseObserver: StreamObserver<LocationMessage>
    ) {
        return GrpcCoroutineWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapConcat { getRequest ->
                    val context = GrpcCoroutineWrapper.contextHolder.get()
                    val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                    val sub =
                        (authentication as JwtAuthenticationToken).token.subject.toInt()

                    locationService.getLocations(sub, getRequest)
                }
                .map {
                    LocationMessage.newBuilder().apply {
                        targetFriendId = it.friendId
                        timestamp = it.timestamp.epochSecond
                        latitude = ByteString.copyFrom(it.latitude)
                        longitude = ByteString.copyFrom(it.longitude)
                    }.build()
                }
        }
    }

}