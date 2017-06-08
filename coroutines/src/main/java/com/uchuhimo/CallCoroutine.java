package com.uchuhimo;

import kotlin.Unit;
import kotlin.coroutines.experimental.EmptyCoroutineContext;
import kotlin.coroutines.experimental.intrinsics.IntrinsicsKt;
import kotlinx.coroutines.experimental.BuildersKt;

public class CallCoroutine {
  public static void main(String[] args) {
    try {
      BuildersKt.runBlocking(EmptyCoroutineContext.INSTANCE, (coroutineScope, continuation) -> {
        CoroutineTestKt.sayHello(continuation);
        return Unit.INSTANCE;
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    CoroutineTestKt.executeBlocking(() -> {
      System.out.println("execute normal");
      return Unit.INSTANCE;
    });
    CoroutineTestKt.executeBlocking(continuation -> {
      System.out.println("execute suspend");
      continuation.resume(Unit.INSTANCE);
      return IntrinsicsKt.getCOROUTINE_SUSPENDED();
    });
  }
}
