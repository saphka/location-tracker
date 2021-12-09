package org.saphka.location.tracker.location.service

import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.lognet.springboot.grpc.security.GrpcSecurity
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper
import org.saphka.location.tracker.location.dao.LocationDAO
import org.saphka.location.tracker.location.grpc.DummyMessage
import org.saphka.location.tracker.location.grpc.LocationMultiRequest
import org.saphka.location.tracker.location.grpc.LocationServiceGrpc
import org.saphka.location.tracker.location.model.Location
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface LocationService {
    fun addLocation(userId: Int, request: LocationMultiRequest): Flux<Location>
}

@Service
class LocationServiceImpl(private val locationDAO: LocationDAO) : LocationService {
    override fun addLocation(userId: Int, request: LocationMultiRequest): Flux<Location> {
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
            .flatMapMany { locationDAO.addLocations(it) }
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
                .flatMapMany { addRequest ->
                    Flux.deferContextual {
                        val context = it.get<Context>(GrpcReactiveWrapper.GRPC_CONTEXT_KEY)
                        val authentication = GrpcSecurity.AUTHENTICATION_CONTEXT_KEY.get(context)
                        val sub =
                            (authentication as JwtAuthenticationToken).token.subject.toInt()
                        locationService.addLocation(sub, addRequest)
                    }
                }
                .collectList()
                .map { DummyMessage.getDefaultInstance() }
        }
    }

}