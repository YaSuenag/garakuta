package com.yasuenag.openjdk.benchmark;

import java.util.*;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;


@State(Scope.Benchmark)
@Threads(Threads.MAX)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgsAppend = {"-Xms8g", "-Xmx8g", "-XX:+AlwaysPreTouch"})
@Warmup(iterations = 1, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class RandMinVal{

  private static final int NUM = 1000;

  private double[] rand;

  @Setup(Level.Invocation)
  public void setup(){
    rand = (new Random()).doubles(NUM).toArray();
  }

  @Benchmark
  public double getMin(){
    double min = Double.MAX_VALUE;
    for(double v : rand){
      min = Math.min(v, min);
    }
    return min;
  }

  @Benchmark
  public double getMax(){
    double max = Double.MIN_VALUE;
    for(double v : rand){
      max = Math.max(v, max);
    }
    return max;
  }

}
