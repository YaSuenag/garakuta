JVMCI vs Pure Java vs JNI vs FFM
===

[JJUG CCC 2026 Fall](https://jjug.doorkeeper.jp/events/199505) での講演「JVMCI - ピーキーな技術よ永遠に」で使用したベンチマークです。ベクトルの内積計算のスループットを Java コード（ここでは "Pure Java" と表現します）、C++ コード 2 種（JNI、FFM Critical 呼び出し）、JVMCI 2 種（`VPDPBUSD` の unroll 数 8（無印）と 4）の 4 実装で比較します。C++ コードは GCC が `-mavxvnni` オプションの効果で AVX2 VNNI の `VPDPBUSD` を使った機械語を生成してくれることを期待しています。

## 講演資料

講演実施後に公開予定

# 必要なもの

* AVX2 VNNI に対応する CPU
* Linux
* Java
    * JVMCI をテストする場合：Java 26
    * JVMCI をテストしない場合：Java 27 以降
* Maven
    * GitHub PAT を settings.xml に設定済みであること（ [ffmasm](https://github.com/YaSuenag/ffmasm) のダウンロードに必要）
* GCC (g++) 11 以降

## 動作確認環境

* OS: Fedora 45 WSL2
    * カーネル：6.18.58.1-microsoft-standard-WSL2
    * libc：glibc-2.44-2.fc45
    * Windows：Windows 11 Professional 26H2
* Java
    * Oracle OpenJDK 26.0.2.1
    * Oracle OpenJDK 27.0.1.0.3
* C++ コンパイラ：gcc-c++-16.2.1-3.fc45
* ハードウェア：Lenovo ThinkPad X13 Gen 6
    * CPU：Intel Core Ultra 5 225U
    * メモリ：32 GB

# ビルド

```
export JAVA_HOME
mvn package
```

# 実行

## 単発実行

Pure Java、JNI、FFM、JVMCI それぞれの計算を 1 回実施し、結果を出力します。各手法の計算の確からしさの評価（特に JVMCI で組んだ機械語が誤っていないかの確認）に使います。

```
mvn exec:exec@test
```

## ベンチマーク

### 全実装方式のスループット計測

```
$JAVA_HOME/bin/java -jar target/dot-product-benchmark-1.0.0.jar
```

### Pure Java と JVMCI の disas 込み

[printasm-directive.json](printasm-directive.json) を `-XX:CompilerDirectivesFile` に与えることで、Pure Java の実装である `javaImpl()` と JVMCI の実装である（ffmasm によってコードインストールされる） `jvmciImpl()` のディスアセンブルを行います。

```
$JAVA_HOME/bin/java -XX:+UnlockDiagnosticVMOptions -XX:CompilerDirectivesFile=printasm-directive.json -jar target/dot-product-benchmark-1.0.0.jar
```

> [!NOTE]
> ディスアセンブルには [hsdis](https://github.com/openjdk/jdk/tree/master/src/utils/hsdis) が必要です。通常は OpenJDK のソースコードからビルドする必要があります。 [hsdis-builder](https://github.com/YaSuenag/hsdis-builder) を使うことで hsdis を簡単にビルドすることができます。

### AsyncProfiler で FlameGraph を取得する

[Async-profiler](https://github.com/async-profiler/async-profiler) を設定することで各ベンチマークの FlameGraph を取得することが可能です。以下は FFM と JVMCI の FlameGraph 収集例です。

```
LD_LIBRARY_PATH=/path/to/asyncprofiler/libdir $JAVA_HOME/bin/java -jar target/dot-product-benchmark-1.0.0.jar -prof "async:output=flamegraph;event=cpu" Test.ffm Test.jvmci
```

# ベンチマーク詳細

ベンチマークは [Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh) の Throughput モードで実施します。関連する Java コードはウォームアップ中に C2（Tier 4）JIT にかかり、テストは HotSpot にとって最高レベルの最適化が施された状態で実施されることを期待しています。

## テスト条件

* 走行時間
    * ウォームアップ： 20 秒×1
    * テスト：10 秒×3
* 利用データ（各ベンチマークの引数として与える配列）
    * 長さ 8192 の `byte[]`
    * [Random](https://docs.oracle.com/javase/jp/26/docs/api/java.base/java/util/Random.html) で生成される乱数で埋める

## ベンチマーク題材

ベクトルの内積計算のスループットを計測します。ロジックは以下のとおりです。これは AVX2 VNNI の `VPDPBUSD` が適用可能なものになっています。

```java
private int javaImpl(byte[] src1, byte[] src2){
  int dotProduct = 0;
  for(int i = 0; i < src1.length; i++){
    dotProduct += Byte.toUnsignedInt(src1[i]) * src2[i];
  }
  return dotProduct;
}
```

## JVMCI によるコードインストール

JVMCI のテスト対象は、Java コード上は以下のように即 `IllegalStateException` をスローするものになっています。ただし、これは `initJVMCI()` で期待する機械語が ffmasm によって生成され、C2 JIT によるコンパイル済みコードとしてコードキャッシュにインストールされることにより上書きされるため、実行時には正常動作するようになります。

```java
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
private int jvmciImpl(byte[] src1, byte[] src2){
  throw new IllegalStateException("This method should be installed by JVMCI");
}
```

> [!NOTE]
> 本ベンチマークで JVMCI でインストールする機械語は配列要素を 256 バイト単位で処理することに特化しています。256 で割り切れないサイズの配列を引数に与えるとクラッシュ等の異常動作を引き起こす可能性があります。

## ベンチマーク対象の引数を単純化するための配慮

JVMCI や FFM で計算対象のベクトルを `byte[]` で受け取るために、メソッドシグネチャは `([B[B)I` にする必要があります。しかし JMH ではベンチマークのエントリポイント（ `@Benchmark` で修飾されたメソッド）の引数には `@State` で修飾されたオブジェクトを渡す必要があるため、これを実現できません。このため、ベンチマークエントリポイントとベンチマーク対象は分離します。

State からの引数抽出とベンチマーク対象呼び出しコストの影響排除、および Pure Java コードのベンチマークエントリポイントへのインライン展開阻止のため、ベンチマーク対象には以下のように `DONT_INLINE` を設定します。

```java
@Benchmark
public int java(VectorHolder holder){
  return javaImpl(holder.src1, holder.src2);
}

@CompilerControl(CompilerControl.Mode.DONT_INLINE)
private int javaImpl(byte[] src1, byte[] src2){
  int dotProduct = 0;
  for(int i = 0; i < src1.length; i++){
    dotProduct += Byte.toUnsignedInt(src1[i]) * src2[i];
  }
  return dotProduct;
}
```
