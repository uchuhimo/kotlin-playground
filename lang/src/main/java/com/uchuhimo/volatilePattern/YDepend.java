package com.uchuhimo.volatilePattern;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

public class YDepend {
  public static AtomicLong invokeTimes = new AtomicLong(0L);

  private volatile boolean aHappened = false;
  private volatile boolean bHappened = false;
  private CountDownLatch latch = new CountDownLatch(2);

  private volatile int invoked = 0;
  private static final AtomicIntegerFieldUpdater<YDepend> INVOKED =
      AtomicIntegerFieldUpdater.newUpdater(YDepend.class, "invoked");

  private void invokeAtLeastOnceAfterAB() {
    invokeTimes.incrementAndGet();
  }

  private void invokeOnceAfterAB() {
    if (INVOKED.compareAndSet(this, 0, 1)) {
      invokeTimes.incrementAndGet();
    }
  }

  public void testAtLeastOnce() {
    new Thread(
            () -> {
              aHappened = true;
              if (bHappened) {
                invokeAtLeastOnceAfterAB();
              }
              latch.countDown();
            })
        .start();
    new Thread(
            () -> {
              bHappened = true;
              if (aHappened) {
                invokeAtLeastOnceAfterAB();
              }
              latch.countDown();
            })
        .start();
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void testOnce() {
    new Thread(
            () -> {
              aHappened = true;
              if (bHappened) {
                invokeOnceAfterAB();
              }
              latch.countDown();
            })
        .start();
    new Thread(
            () -> {
              bHappened = true;
              if (aHappened) {
                invokeOnceAfterAB();
              }
              latch.countDown();
            })
        .start();
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 10_000; i++) {
      new YDepend().testAtLeastOnce();
    }
    System.out.println(invokeTimes.get());
    System.out.println(System.currentTimeMillis() - startTime);
    invokeTimes.set(0L);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < 10_000; i++) {
      new YDepend().testOnce();
    }
    System.out.println(invokeTimes.get());
    System.out.println(System.currentTimeMillis() - startTime);
  }
}
