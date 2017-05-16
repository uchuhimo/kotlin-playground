package com.uchuhimo.layout;

import java.util.concurrent.atomic.AtomicLong;

class PaddedAtomicLong extends AtomicLong {
  public volatile long p1, p2, p3, p4, p5, p6, p7 = 7L;
}
