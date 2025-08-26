Attach API のシグナルハンドラチェックをキャンセルする JVMTI エージェント
===

Linux 版 [Attach API](https://docs.oracle.com/javase/jp/24/docs/api/jdk.attach/module-summary.html) がアタッチ時に内部的に実施するシグナルハンドラチェックをキャンセルします。

シグナルの実装が不完全な環境（WSL 1 など : [参考](https://github.com/microsoft/WSL/issues/1880) : 2025/08 現在）で強制的に [jcmd](https://docs.oracle.com/javase/jp/24/docs/specs/man/jcmd.html) を実行したい場合などに便利です。

# 詳細

[JDK-8342449](https://bugs.openjdk.org/browse/JDK-8342449) の修正が取り込まれたことにより、JDK 24 から Linux の Attach API ではアタッチ対象プロセスに `SIGQUIT` のシグナルハンドラが設定されていることをアタッチ前に確認するようになりました。これによりアタッチ対象プロセスで `SIGQUIT` がトラップ可能でない場合、Attach API を利用するプログラムは [AttachNotSupportedException](https://docs.oracle.com/javase/jp/24/docs/api/jdk.attach/com/sun/tools/attach/AttachNotSupportedException.html) で異常終了することがあります。

> [!NOTE]
> `SIGQUIT` 発行タイミングなど Attach API の挙動の詳細については JJUG CCC 2013 Spring で発表した [Serviceability Tools の裏側](https://www.slideshare.net/slideshow/serviceability-tools/20966504) をご覧ください。

sigforce は JDK-8342449 により追加されたシグナルハンドラチェックをキャンセルするよう、Attach API で JVM 操作を担う `sun.tools.attach.VirtualMachineImpl` の Linux 向け実装のバイトコードを変換します。C++ で作成したネイティブエージェントライブラリ（ `libsigforce.so` ）と Java エージェント（ `sigforce-0.1.0.so` ）のいずれかで実現します。

# ビルド

## 必要なもの

* JDK 24 以降
* Maven

Java エージェントの場合は上記のみで OK ですが、ネイティブエージェントの場合はこれらに加えて以下も必要です。

* GNU make
* g++
* GNU Binutils

## コマンド

### ネイティブエージェントライブラリの場合

```
export JAVA_HOME
make
```

### Java エージェントの場合

```
mvn package
```

# 使い方

Attach API を使うプログラムの実行時に `libsigforce.so` か `sigforce-0.1.0.jar` のいずれかを仕込みます。以下、 `jcmd` に仕込む場合の例を示します。

## ネイティブエージェントライブラリの場合

### `LD_LIBRARY_PATH` に `libsigforce.so` が入っている場合

```
jcmd -J-agentlib:sigforce ...
```

### `libsigforce.so` を直接指定する場合

```
jcmd -J-agentpath:/path/to/libsigforce.so ...
```

## Java エージェントの場合

```
jcmd -J-javaagent:/path/to/sigforce-0.1.0.jar ...
```

# カラクリ

## ネイティブエージェントライブラリの場合

1. [ClassFileLoadHook](https://docs.oracle.com/javase/jp/24/docs/specs/jvmti.html#ClassFileLoadHook) JVMTI イベントで `sun.tools.attach.VirtualMachineImpl` のロードを検知し、このクラスの（元々の）バイナリデータを取得する
2. `libsigforce.so` に埋め込んだ [com.yasuenag.garakuta.sigforce.Transformer](src/main/java/com/yasuenag/garakuta/sigforce/Transformer.java) を [DefineClass](https://docs.oracle.com/javase/jp/24/docs/specs/jni/functions.html#defineclass) JNI 関数でロードし、このクラスが提供する `transform` メソッドを呼び出す
3. [ClassFile API](https://docs.oracle.com/javase/jp/24/vm/jvm-apis.html#GUID-CA6D8301-F38A-46BE-90B8-903EBDB449F3) を使って `VirtualMachineImpl::checkCatchesAndSendQuitTo` の内容を、ただ `VirtualMachineImpl::sendQuitTo` （アタッチ対象プロセスに `SIGQUIT` を発行する JNI 実装）を呼び出し、 `true` を返すだけのバイトコードに置換する
4. `ClassFileLoadHook` の `new_class_data` 引数に 3. で生成したバイトコードを反映したバイナリデータを設定し、JVM にロードさせる

## Java エージェントの場合

1. [com.yasuenag.garakuta.sigforce.Agent](src/main/java/com/yasuenag/garakuta/sigforce/Agent.java) の `premain()` の引数に渡される `Instrumentation` の [addTransformer](https://docs.oracle.com/javase/jp/24/docs/api/java.instrument/java/lang/instrument/Instrumentation.html#addTransformer(java.lang.instrument.ClassFileTransformer)) に `com.yasuenag.garakuta.sigforce.Transformer::transform` を呼び出すよう、トランスフォーマ（実装クラスは `com.yasuenag.garakuta.sigforce.Agent` ）を登録する
2. 1.のトランスフォーマが `sun.tools.attach.VirtualMachineImpl` に対して呼び出されると、 [ClassFileTransformer::transform](https://docs.oracle.com/javase/jp/24/docs/api/java.instrument/java/lang/instrument/ClassFileTransformer.html#transform(java.lang.ClassLoader,java.lang.String,java.lang.Class,java.security.ProtectionDomain,byte%5B%5D)) が `com.yasuenag.garakuta.sigforce.Transformer` を呼び出し、 `VirtualMachineImpl::checkCatchesAndSendQuitTo` の内容を、ただ `VirtualMachineImpl::sendQuitTo` （アタッチ対象プロセスに `SIGQUIT` を発行する JNI 実装）を呼び出し、 `true` を返すだけのバイトコードに置換する
3. 変換したバイトコードを `ClassFileTransformer::transform` の戻り値に設定し、変換後クラスとして JVM にロードさせる

# 免責事項

将来の OpenJDK 実装や OpenJDK ディストリビューション独自の変更により `VirualMachineImpl` の実装が変わる場合、この JVMTI エージェントが動作しなくなる可能性があります。
