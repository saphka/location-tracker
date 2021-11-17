package org.saphka.location.tracker.commons

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper
import reactor.core.publisher.Mono

class GrpcReactiveWrapperTest {

    @Test
    fun `Success test`() {
        val observer = mock(StringStreamObserver::class.java)

        GrpcReactiveWrapper.wrap(
            "dummy",
            observer
        ) {
            it.map { "not so dummy" }
        }

        verify(observer).onNext("not so dummy")
        verify(observer).onCompleted()
        verify(observer, times(0)).onError(any())
    }

    @Test
    fun `Error test`() {
        val observer = mock(StringStreamObserver::class.java)

        GrpcReactiveWrapper.wrap(
            "dummy",
            observer
        ) {
            it.map { throw Status.INVALID_ARGUMENT.asRuntimeException() }
        }

        verify(observer).onError(any(StatusRuntimeException::class.java))
        verify(observer, times(0)).onCompleted()
        verify(observer, times(0)).onNext(any())
    }

    @Test
    fun `Error in map test`() {
        val observer = mock(StringStreamObserver::class.java)

        GrpcReactiveWrapper.wrap(
            "dummy",
            observer
        ) {
             throw Status.ABORTED.asRuntimeException()
        }

        verify(observer).onError(any(StatusRuntimeException::class.java))
        verify(observer, times(0)).onCompleted()
        verify(observer, times(0)).onNext(any())
    }

    @Test
    fun `Empty test`() {
        val observer = mock(StringStreamObserver::class.java)

        GrpcReactiveWrapper.wrap(
            "dummy",
            observer
        ) {
            it.flatMap { Mono.empty<String>() }
        }

        verify(observer).onCompleted()
        verify(observer, times(0)).onNext(any())
        verify(observer, times(0)).onError(any())
    }

}

private interface StringStreamObserver : StreamObserver<String>