Foreign Function & Memory APIでハンドアセンブルしたコードを実行する
===================

Java 19 で Preview になった Foreign Function & Memory API ( [JEP 424](https://openjdk.org/jeps/424) ) を使って、Java のみでハンドアセンブルしたコードを実行します。

# 必要なもの

* JDK 19
* Maven
* Linux

# 処理の流れ

1. Foreign Function を使って `mmap()` で実行ビット（ `PROT_EXEC` ）の立ったメモリ空間を作成する
2. `EAX` と `ECX` 、 `CPUID` 命令実行後の `EAX` ～ `EDX` を格納するメモリへのポインタを引数にとり、 `CPUID` を実行する関数をハンドアセンブルし 1. で確保したメモリ（Foreign Memory）へ格納する
3. 2. の先頭アドレスを指す `MethodHandle` を取得する
4. CPU モデル取得のため、 `EAX` に `0x80000002` から `0x0x80000004` を設定して 3. で取得した `MethodHandle` 経由で `CPUID` を実行する
5. Foreign Function を使って `munmap()` で 1. で作成したメモリ空間を破棄する
6. 結果の `byte[]` を結合し、 `US_ASCII` として 1 つの文字列に結合する

# コンパイル

```bash
$ mvn package
```

# 動かしてみる

```bash
$ $JAVA_HOME/bin/java --enable-preview -jar ffm-cpumodel-0.1.0.jar
WARNING: A restricted method in java.lang.foreign.Linker has been called
WARNING: java.lang.foreign.Linker::nativeLinker has been called by the unnamed module
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for this module

Intel(R) Core(TM) i3-8145U CPU @ 2.10GHz
```
