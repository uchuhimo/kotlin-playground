package com.uchuhimo

import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class Benchmark {
    @Benchmark
    fun createClosure() {
        val function = { 1 + 1 }
        function()
    }

    @Benchmark
    fun createBlockingCoroutine() = runBlocking {

    }

    @Benchmark
    @OperationsPerInvocation(1000)
    fun createCoroutine() {
        startCoroutine {
        }
    }

    fun startCoroutine(block: suspend () -> Unit) {
        repeat(1000) {
            block.startCoroutine(NoopContinuation)
        }
    }

    object NoopContinuation : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWithException(exception: Throwable) {
        }

        override fun resume(value: Unit) {
        }
    }
}
