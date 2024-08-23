Foreign Function & Memory APIでハンドアセンブルしたコードを実行する
===================

Java 22 で正式導入された Foreign Function & Memory API を使って、Java のみでハンドアセンブルしたコードを実行します。

# 必要なもの

* JDK 22
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

```
$ $JAVA_HOME/bin/java -jar target/ffm-cpumodel-0.1.1.jar
Intel(R) Core(TM) i3-8145U CPU @ 2.10GHz
```
