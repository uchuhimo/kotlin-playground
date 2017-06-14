package com.uchuhimo

import kotlinx.coroutines.experimental.AbstractCoroutine
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Delay
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.NonCancellable
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.run
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.selects.select
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.yield
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.system.measureTimeMillis

val scheduledExecutor = ScheduledThreadPoolExecutor(1, { r -> Thread(r).apply { isDaemon = true } })

object CommonPoolWithDelay : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        CommonPool.dispatch(context, block)
    }

    override fun scheduleResumeAfterDelay(time: Long, unit: TimeUnit, continuation: CancellableContinuation<Unit>) {
        println("${Thread.currentThread().name}: in CommonPoolWithDelay")
        scheduledExecutor.schedule({ continuation.resume(Unit) }, time, unit)
    }
}

suspend fun sayHello() {
    return suspendCoroutine { cont ->
        println("${Thread.currentThread().name}: hello")
        cont.resume(Unit)
    }
}

fun executeBlocking(block: suspend () -> Unit) {
    runBlocking(block = { block() })
}

fun executeBlocking(block: () -> Unit) {
    block()
}

fun main1(args: Array<String>) = runBlocking<Unit> {
    println("${Thread.currentThread().name}: run")
    val job: Job = launch(CommonPoolWithDelay) {
        delay(1000L)
        println("${Thread.currentThread().name}: delay")
    }
    sayHello()
    job.join()
    yield()
    val launch = launch(context) {
        try {
            run(NonCancellable) {
                delay(1000L)
            }
        } catch (e: CancellationException) {
            println("cancel")
        }
    }
    produce<Int>(CommonPool) {
        for (x in 1..5) send(x * x)
    }
    delay(500L)
    launch.cancel()
}

var counter = 0
val counterContext = newSingleThreadContext("CounterContext")
val mutex = Mutex()

suspend inline fun Mutex.withLock(action: () -> Unit) {
    lock()
    try {
        action()
    } finally {
        unlock()
    }
}

suspend fun massiveRun(context: CoroutineContext, action: suspend () -> Unit) {
    val n = 10_000 // number of coroutines to launch
    val k = 10_000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        val jobs = List(n) {
            launch(context) {
                repeat(k) { action() }
            }
        }
        jobs.forEach { it.join() }
    }
    println("Completed ${n * k} actions in $time ms")
}

fun main2(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) {
        mutex.withLock {
            counter++
        }
    }
    println("Counter = ${counter}")
}

// Message types for counterActor
sealed class CounterMsg

object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: SendChannel<Int>) : CounterMsg() // a request with reply

// This function launches a new counter actor
fun counterActor() = actor<CounterMsg>(CommonPool) {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.send(counter)
        }
    }
}

fun main3(args: Array<String>) = runBlocking<Unit> {
    val counter = counterActor() // create the actor
    massiveRun(CommonPool) {
        counter.send(IncCounter)
    }
    val response = Channel<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.receive()}")
    counter.close() // shutdown the actor
}

suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
    select<Unit> {
        // <Unit> means that this select expression does not produce any result
        fizz.onReceive { value ->
            // this is the first select clause
            println("fizz -> '$value'")
        }
        buzz.onReceive { value ->
            // this is the second select clause
            println("buzz -> '$value'")
        }
    }
}

fun main(args: Array<String>) = runBlocking<Unit> {
    val time = measureTimeMillis {
        val one = async(CommonPool) { 1 }
        val two = async(CommonPool) { 2 }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

class TestCoroutine<in T> : AbstractCoroutine<T>(true) {
    override val parentContext: CoroutineContext
        get() = EmptyCoroutineContext

    override fun afterCompletion(state: Any?, mode: Int) {
        super.afterCompletion(state, mode)
    }
}