乱数ジェネレータ性能比較
===

Java の [SecureRandom](https://docs.oracle.com/javase/jp/22/docs/api/java.base/java/security/SecureRandom.html) を利用して、以下の実装のスループットを比較します。

* Java 標準搭載
    * `NativePRNG`
    * `NativePRNGBlocking`
    * `NativePRNGNonBlocking`
* Intel 64 命令セット
    * `RDRAND`
    * `RDSEED`
* Linux システムコール
    * `getramdom(2)` （ブロッキングモード）
    * `getramdom(2)` （ノンブロッキングモード）

# 前提条件

* Java 22 以降
* Linux カーネル 3.17 以降
* glibc 2.25 以降

# 比較対象詳細

## Java 標準搭載

[SecureRandom 数値生成アルゴリズム](https://docs.oracle.com/javase/jp/22/docs/specs/security/standard-names.html#securerandom-number-generation-algorithms) で紹介されている、Linux の random 系デバイスファイルをソースとする疑似乱数ジェネレータを使用します。 `java.security.egd` システムプロパティや `securerandom.source` セキュリティプロパティが設定されていないデフォルト状態では、各実装（アルゴリズム）は以下をソースとする組み合わせで初期化されます。

OpenJDK 22 GA のソースコード： https://github.com/openjdk/jdk/blob/jdk-22-ga/src/java.base/unix/classes/sun/security/provider/NativePRNG.java#L130

* `NativePRNG`
    * 乱数シード： `/dev/random`
    * 乱数生成（next 系メソッドで利用）： `/dev/urandom`
* `NativePRNGBlocking`
    * 乱数シード： `/dev/random`
    * 乱数生成（next 系メソッドで利用）： `/dev/random`
* `NativePRNGNonBlocking`
    * 乱数シード： `/dev/urandom`
    * 乱数生成（next 系メソッドで利用）： `/dev/urandom`

## Intel 64 命令セット

`RDRAND` と `RDSEED` を使用します。どちらの命令も最大で 8 バイト（64 bit）の書き込みのサポートとなるため、今回実験する 256 バイトの乱数生成を行うために、繰り返し各命令を実行します。

命令実行のため [ffmasm](https://github.com/YaSuenag/ffmasm) を用いて各命令実行用の機械語を生成します。

## Linux システムコール

[getrandom(2)](https://man7.org/linux/man-pages/man2/getrandom.2.html) で乱数を生成します。manpage より、256 バイトまではシグナル割り込みを受けず値を即返すことができるため、このベンチマークでは 256 バイトの乱数生成を試験条件とします。

Java では `getrandom(2)` を呼び出す API は用意されないため、Foreign Function & Memory API で実行します。

# 試験条件

* Java ヒープサイズ 8GB（ `-Xms8g -Xmx8g` ）
* GC アルゴリズムは Epsilon
* `-XX:+AlwaysPreTouch` を付与
* 256 バイトの乱数を 10 秒間に生成するスループットを 3 回測定する
* Intel 64 命令セットは ffmasm で直接実行する
    * コード生成は [FFMHelper.java](src/main/java/com/yasuenag/garakuta/randbench/internal/FFMHelper.java) で行う
* `getrandom(2)` は FFM で呼び出す
* FFM で呼び出す `RDRAND` / `RDSEED` / `getrandom(2)` は critical function として定義し、生成した乱数の書き込み先は Java ヒープ上の `byte[]` とする

# ビルド

```
mvn package
```

# 実行

## テスト実行

FFM で呼び出す `RDRAND` / `RDSEED` / `getrandom(2)` （ `GRND_RANDOM` つき） / `getrandom(2)` （ `GRND_NONBLOCK` ）については生成した乱数の一部を確認できます。

```
mvn exec:java
```

## ベンチマーク実行

`mvn package` で生成した JAR は JMH と結合済みのため、実行するだけでベンチマークできます。

```
java -jar target/randbench-0.1.0.jar
```
