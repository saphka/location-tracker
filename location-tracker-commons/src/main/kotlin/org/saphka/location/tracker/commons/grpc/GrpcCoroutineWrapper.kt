package org.saphka.location.tracker.commons.grpc

import io.grpc.Context
import io.grpc.Status
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.function.Function

class GrpcCoroutineWrapper {
    companion object {
        val contextHolder = ThreadLocal<Context>()

        fun <TRequest, TResponse> wrap(
            request: TRequest,
            responseObserver: StreamObserver<TResponse>,
            delegate: Function<Flow<TRequest>, Flow<TResponse>>
        ) {
            try {
                contextHolder.set(Context.current().fork())
                val rxRequestFlow = delegate
                    .apply(flowOf(request))

                runBlocking(contextHolder.asContextElement()) {
                    launch {
                        rxRequestFlow
                            .onCompletion {
                                if (it == null)
                                    responseObserver.onCompleted()
                            }
                            .catch {
                                responseObserver.onError(Status.fromThrowable(it).asRuntimeException())
                            }
                            .collect {
                                responseObserver.onNext(it)
                            }
                    }
                }
            } catch (throwable: Throwable) {
                responseObserver.onError(Status.fromThrowable(throwable).asRuntimeException())
            }
        }
    }
}