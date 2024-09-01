package com.yasuenag.garakuta.randbench;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgsAppend = {"-Xms8g", "-Xmx8g", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC", "-XX:+AlwaysPreTouch", "--enable-native-access=ALL-UNNAMED"})
@Warmup(iterations = 1, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class Main{

  // According to manpage of getrandom(2), urandom can always return
  // up to 256 bytes without any interrupts by signals.
  public static final int RAND_SIZE = 256;

  private SecureRandom rand;

  @Param({
          "NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking",
          "RdRand", "RdSeed",  // Intel 64 instructions
          "GetRandomBlocked", "GetRandomNonBlocked"  // getrandom()
        })
  private String algo;

  private byte[] bytes;

  static{
    Security.addProvider(new RandBenchProvider());
  }

  @Setup
  public void setup(){
    bytes = new byte[RAND_SIZE];
    try{
      rand = SecureRandom.getInstance(algo);
    }
    catch(NoSuchAlgorithmException e){
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public void fillRandom(){
    rand.nextBytes(bytes);
  }

  private static void showRandomValue(String algo) throws Exception{
    var inst = new Main();
    inst.algo = algo;
    inst.setup();
    inst.fillRandom();
    System.out.printf("%s: %02x %02x %02x ...\n", algo, inst.bytes[0], inst.bytes[1], inst.bytes[2]);
  }

  public static void main(String[] args) throws Exception{
    showRandomValue("RdRand");
    showRandomValue("RdSeed");
    showRandomValue("GetRandomBlocked");
    showRandomValue("GetRandomNonBlocked");
  }

}
