package org.saphka.location.tracker.user.service

import io.grpc.Context
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import reactor.core.publisher.Mono
import java.util.function.Function

class GrpcUtil {
    companion object {
        const val GRPC_CONTEXT_KEY = "grpc"

        fun <TRequest, TResponse> processCall(
            request: TRequest,
            responseObserver: StreamObserver<TResponse>,
            delegate: Function<Mono<TRequest>, Mono<TResponse>>
        ) {
            try {
                val currentGrpcContext = Context.current()
                val rxRequest =
                    if (request != null) Mono.just(request) else Mono.empty()

                val rxResponse =
                    delegate
                        .apply(rxRequest)
                        .contextWrite {
                            it.put(GRPC_CONTEXT_KEY, currentGrpcContext)
                        }
                rxResponse.subscribe(
                    { value: TResponse ->
                        // Don't try to respond if the server has already canceled the request
                        if (responseObserver is ServerCallStreamObserver<*> && (responseObserver as ServerCallStreamObserver<*>).isCancelled) {
                            return@subscribe
                        }
                        responseObserver.onNext(value)
                    },
                    { throwable: Throwable ->
                        responseObserver.onError(
                            prepareError(
                                throwable
                            )
                        )
                    },
                    { responseObserver.onCompleted() }
                )
            } catch (throwable: Throwable) {
                responseObserver.onError(prepareError(throwable))
            }
        }

        private fun prepareError(throwable: Throwable): Throwable {
            return if (throwable is StatusException || throwable is StatusRuntimeException) {
                throwable
            } else {
                Status.fromThrowable(throwable).asException()
            }
        }
    }
}