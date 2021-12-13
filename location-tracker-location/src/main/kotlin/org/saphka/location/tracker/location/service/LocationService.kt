package org.saphka.location.tracker.location.service

import com.google.protobuf.ByteString
import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper
import org.saphka.location.tracker.location.dao.LocationDAO
import org.saphka.location.tracker.location.grpc.*
import org.saphka.location.tracker.location.model.Location
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface LocationService {
    fun addLocation(userId: Int, request: LocationMultiRequest): Mono<Int>
    fun getLocations(userId: Int, request: PageRequest): Flux<Location>
}

private const val DEFAULT_PAGE_SIZE = 100

@Service
class LocationServiceImpl(private val locationDAO: LocationDAO) : LocationService {
    override fun addLocation(userId: Int, request: LocationMultiRequest): Mono<Int> {
        return Mono.just(request.locationList)
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
            .flatMap { locationDAO.addLocations(it) }
    }

    override fun getLocations(userId: Int, request: PageRequest): Flux<Location> {
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
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMap { addRequest ->
                    Mono.deferContextual {
                        val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            (authentication as JwtAuthenticationToken).token.subject.toInt()
                        locationService.addLocation(sub, addRequest)
                    }
                }
                .map { DummyMessage.getDefaultInstance() }
        }
    }

    override fun getCurrentUserFriendsLocations(
        request: PageRequest,
        responseObserver: StreamObserver<LocationMultiResponse>
    ) {
        return GrpcReactiveWrapper.wrap(
            request,
            responseObserver
        ) { req ->
            req
                .flatMapMany { getRequest ->
                    Flux.deferContextual {
                        val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            (authentication as JwtAuthenticationToken).token.subject.toInt()

                        locationService.getLocations(sub, getRequest)
                    }
                }
                .collectList()
                .map { locations ->
                    LocationMultiResponse.newBuilder().apply {
                        locations.forEach {
                            this.addLocationBuilder().apply {
                                targetFriendId = it.friendId
                                timestamp = it.timestamp.epochSecond
                                latitude = ByteString.copyFrom(it.latitude)
                                longitude = ByteString.copyFrom(it.longitude)
                            }
                        }
                    }.build()
                }
        }
    }

}