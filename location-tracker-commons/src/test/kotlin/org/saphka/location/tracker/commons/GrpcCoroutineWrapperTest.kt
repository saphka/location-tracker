package org.saphka.location.tracker.commons

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.saphka.location.tracker.commons.grpc.GrpcCoroutineWrapper

class GrpcCoroutineWrapperTest {

    @Test
    fun `Success test`() {
        val observer = mock(StringStreamObserver::class.java)

        runBlocking {
            GrpcCoroutineWrapper.wrap(
                "dummy",
                observer
            ) {
                it.map { "not so dummy" }
            }
        }

        verify(observer, times(0)).onError(any())
        verify(observer).onCompleted()
        verify(observer).onNext("not so dummy")
    }

    @Test
    fun `Error test`() {
        val observer = mock(StringStreamObserver::class.java)

        runBlocking {
            GrpcCoroutineWrapper.wrap(
                "dummy",
                observer
            ) {
                it.map { throw Status.INVALID_ARGUMENT.asRuntimeException() }
            }
        }

        verify(observer, times(0)).onCompleted()
        verify(observer, times(0)).onNext(any())
        verify(observer).onError(any(StatusRuntimeException::class.java))
    }

    @Test
    fun `Error in map test`() {
        val observer = mock(StringStreamObserver::class.java)

        runBlocking {
            GrpcCoroutineWrapper.wrap(
                "dummy",
                observer
            ) {
                throw Status.ABORTED.asRuntimeException()
            }
        }

        verify(observer, times(0)).onCompleted()
        verify(observer, times(0)).onNext(any())
        verify(observer).onError(any(StatusRuntimeException::class.java))
    }

    @Test
    fun `Empty test`() {
        val observer = mock(StringStreamObserver::class.java)

        runBlocking {
            GrpcCoroutineWrapper.wrap(
                "dummy",
                observer
            ) {
                it.flatMapConcat { emptyFlow() }
            }
        }

        verify(observer, times(0)).onNext(any())
        verify(observer, times(0)).onError(any())
        verify(observer).onCompleted()
    }

}

private interface StringStreamObserver : StreamObserver<String>