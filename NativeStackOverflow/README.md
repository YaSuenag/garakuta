# 概要

ネイティブライブラリ内で意図的にネイティブレベルのスタックオーバーフローを起こすJNIライブラリです。hs_errが出ないクラッシュを起こします。

昔はこれでhs_errが吐けたのですが、LinuxカーネルかSolarisかの脆弱性の関係で `sigaltstack` を使ったエラーハンドリングがなくなったそうです。詳しい経緯は [OpenJDKのML](http://mail.openjdk.java.net/pipermail/hotspot-runtime-dev/2011-August/002354.html) をどうぞ。

# 対応環境

* Linux x86_64
* Linux AArch64

# ネイティブレベルのスタックオーバーフローの起こし方

`alloca()` を使うとかいろいろありますが、このサンプルでは確実にスタックのガードページを触るために、アセンブラでプロローグ後にスタックポインタを上へ向かって順次書き込んでいきます。

# 試してみる

```
$ cd build
$ cmake ..
$ make
$ java -Djava.library.path=. -jar NativeStackOverflow.jar
```

# FFM で発生するネイティブレベルのスタックオーバーフロー

Java 22 から導入された Foreign Function & Memory API を使って、Javaレイヤの処理中にネイティブレベルのスタックオーバーフローが発生する例です。 [ffm](ffm) を使用します。

## 対応環境

* Linux x86_64
* Java 22 以降

## カラクリ

[ffmasm](https://github.com/YaSuenag/ffmasm) を使って、main スレッドのスタックアドレスを `RSP` レジスタから直接取得します。それを基に `/proc/self/maps` からスタックアドレス範囲とその前に位置するガードスタックを取得します。  
その後、スレッド状態が `_thread_in_java` に遷移するのに十分なスタック量をガードページとの境界に足した（スタックに余裕を持たせた）後に Java メソッドを upcall することで Java レイヤでネイティブレベルのスタックオーバーフローを発生させます。

「スレッド状態が `_thread_in_java` に遷移するのに十分なスタック量」は [Main.java](src/main/java/com/yasuenag/garakuta/nativestackoverflow/Main.java) の以下の部分で設定します。現状 `1024` に設定していますが、これは Oracle OpenJDK 22.0.1 で確認した値です。他の JDK・バージョンでは異なる可能性があります。

```java
private static MethodHandle createStackOverflowStub(long guardPage) throws Exception{
  // Add some bytes to stack enough to transit to _thread_in_java state
  long newRSP = guardPage + 1024;
```

## 試してみる

```
mvn package
mvn exec:java
```

スレッド状態の確認は `jhsdb` で行います。

```
coredumpctl dump -o core
$JAVA_HOME/bin/jhsdb jstack --exe $JAVA_HOME/bin/java --core core
```
