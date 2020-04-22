# 概要

[JVMTI](https://docs.oracle.com/en/java/javase/14/docs/specs/jvmti.html) で実装した簡易的なJavaのデッドロック検知器です。検知するのは `synchronized` ステートメントで発生するデッドロックのみです。

# 性能への影響

このライブラリは [MonitorContendedEnter](https://docs.oracle.com/en/java/javase/14/docs/specs/jvmti.html#MonitorContendedEnter) をフックして、そのたびに [GetObjectMonitorUsage()](https://docs.oracle.com/en/java/javase/14/docs/specs/jvmti.html#GetObjectMonitorUsage) と [GetCurrentContendedMonitor()](https://docs.oracle.com/en/java/javase/14/docs/specs/jvmti.html#GetCurrentContendedMonitor) を使用して、ロックに関与するスレッドとモニタをすべて探索します。そのため、関与スレッド数が多くなるほど O(N) でアプリケーションスループットへ悪影響を与えるのでご注意ください。これらの JVMTI 関数は JDK 14 以下では Safepoint で実行されます（JDK 15 以降は Thread Local Handshake を使うので、若干影響は緩和されます）。

# 試してみる

## ビルド

```
$ export JAVA_HOME
$ cd build
$ cmake ..
$ make
```

## 実行

[example/DeadLock.java](example/DeadLock.java) を例にします。このサンプルは 3 スレッドが関与するデッドロックを起こすのものです。

```
$JAVA_HOME/bin/java -agentpath:path/to/libdldetector.so DeadLock
dldetector: loaded
Thread A holds lock A
Thread C holds lock C
Thread B holds lock B
Thread B waits lock C
Thread A waits lock B
Thread C waits lock A
dldetector: Dead lock was occurred!
```

Windows以外の環境では、上記メッセージの後にスレッドダンプが表示されます。

# Windowsの場合

`%JAVA_HOME%` 設定後、Visual Studioで java-deadlock-detector を開いてください。CMakeプロジェクトとして認識されます。Visual Studioでビルドすると `out\build` 配下にバイナリが出力されます。
