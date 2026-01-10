# 概要

ネイティブライブラリ内で意図的にSEGVを起こすJNIライブラリです。Javaのクラッシュ時の挙動の確認などにご利用ください。

# 試してみる

## ビルド

```
$ export JAVA_HOME
$ cd build
$ cmake ..
$ make
```

## 実行

### JNI関数内でのSEGV

```
$ java -jar NativeSEGV.jar
```

### libc関数内でのSEGV

```
$ java -jar NativeSEGV.jar -l
```

### 任意のタイミングでクラッシュしたい場合

`-s` オプションを追加してください

```
$ java -jar NativeSEGV.jar -s
$ java -jar NativeSEGV.jar -l -s
```

# Windowsの場合

`%JAVA_HOME%` 設定後、Visual StudioでNativeSEGVを開いてください。CMakeプロジェクトとして認識されます。Visual Studioでビルドすると `out\build` 配下にバイナリが出力されます。

# コンテナ内でのSEGV

OpenJDK 17のオフィシャルイメージ（Alpine版）の中でクラッシュさせることもできます。このとき、hs_errログは `-XX:+ErrorFileToStderr` により標準エラー出力に出力されます。

```
$ cd container
$ buildah bud --layers -t javacrash:jdk17
$ podman run -it --rm javacrash:jdk17
```

# FFM での SEGV

Java 22 から Foreign Function & Memory API が導入されました。 `NULL` を表現する [MemorySegment.NULL](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/foreign/MemorySegment.html#NULL) に書き込みオペレーションを実行したときに SEGV を起こします。

```
cd ffm
java SegvInFFM.java
```

upcall 内での SEGV を確認するには [upcall](ffm/upcall) を実行します。

```
cd ffm/upcall
mvn package
mvn exec:java
```

> [!TIP]
> クラッシュしたときのコアイメージなど `jhsdb jstack` でコールスタックを取得しようとすると、動的生成される Upcall 用スタブコードから下のコールスタックを取得できない可能性があります。この問題は [JDK-8339307](https://bugs.openjdk.org/browse/JDK-8339307) で修正されており、Java 24 から正式に織り込まれる予定です。

# フレームレスな関数での SEGV

コンパイラの最適化などによりフレームポインタが保存されないかつ DWARF も存在しない関数が生成されることがあります。 [frameless](frameless) ではそのような関数内で SEGV を起こします。

> [!IMPORTANT]
> frameless は Linux でのみ動作します。

```
cd frameless
make
java -Djava.library.path=. SegvInFrameless
```

# FFM で呼び出した vDSO 内での SEGV

FFM の例とほぼ同じですが、こちらは vDSO で提供される `gettimeofday(2)` に不正な引数を渡してクラッシュさせるものです。

```
cd ffm
java SegvInGettimeofdayWithFFM.java
```
