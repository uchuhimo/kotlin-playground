package com.uchuhimo.volatilePattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

public class YDependBenchmark {
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public void testOnce() {
    new YDepend().testOnce();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public void testAtLeastOnce() {
    new YDepend().testAtLeastOnce();
  }
}
