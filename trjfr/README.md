JFRのメソッドプロファイリングを時間指定で抜き出す
===================

# 必要なもの

* JDK 17以降
* Maven

# コンパイル

```
$ mvn package
```

# 動かしてみる

```
$ java -jar trjfr-1.0.0.jar <-s <開始時間（ISO 8601形式）>> <-e <終了時間（ISO 8601形式）>> <フライトレコードファイル>
```

* 開始時間（ `-s` ）と終了時間（ `-e` ）はオプションです
* 開始時間と終了時間は **含まれません**

# 出力例

CSV形式で、メソッド、イベント数、パーセンテージの順で出力されます。

```
java.util.Random.nextBytes([B)V, 35, 53.03%
java.util.Base64$Encoder.encodeBlock([BII[BIZ)V, 29, 43.94%
sun.nio.ch.IOUtil.read(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JZILsun/nio/ch/NativeDispatcher;)I, 1, 1.52%
org.apache.tomcat.util.net.SocketWrapperBase.populateReadBuffer(Ljava/nio/ByteBuffer;)I, 1, 1.52%
```
