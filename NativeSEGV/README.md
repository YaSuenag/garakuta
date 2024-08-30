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
$ java -Djava.library.path=. -jar NativeSEGV.jar
```

### libc関数内でのSEGV

```
$ java -Djava.library.path=. -jar NativeSEGV.jar -l
```

### 任意のタイミングでクラッシュしたい場合

`-s` オプションを追加してください

```
$ java -Djava.library.path=. -jar NativeSEGV.jar -s
$ java -Djava.library.path=. -jar NativeSEGV.jar -l -s
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
