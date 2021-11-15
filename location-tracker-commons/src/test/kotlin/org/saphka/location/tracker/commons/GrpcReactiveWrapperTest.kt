package org.saphka.location.tracker.commons

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.saphka.location.tracker.commons.grpc.GrpcReactiveWrapper

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
    }

}

private interface StringStreamObserver : StreamObserver<String>