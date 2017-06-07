package com.uchuhimo.volatilePattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class YDependBenchmark {
  @Benchmark
  public void testOnce() {
    new YDepend().testOnce();
  }

  @Benchmark
  public void testAtLeastOnce() {
    new YDepend().testAtLeastOnce();
  }
}
