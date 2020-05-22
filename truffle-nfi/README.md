# 概要

[GraalVM](https://www.graalvm.org/) の言語実装フレームワークである [Truffle](https://github.com/oracle/graal/tree/master/truffle/) には **NFI** と呼ばれるネイティブ関数呼び出し用のインターフェースが含まれています。しかし、これは Truffle を利用する言語が内部的に利用する「言語」として定義されているものであり、Polyglot アプリケーションが自由に使えるものではありません。

nfiwrapper は NFI を外から Polyglot 呼び出しで使えるようにするラッパーです。これにより、C 言語関数を [JNI](https://docs.oracle.com/javase/jp/11/docs/specs/jni/index.html) に頼らずに直接実行することができます。

# 前提条件

GraalVM 20.1.0　JDK 11 版

* JDK 8 版 で使いたい場合はソースコードの変更（一部で `var` を使っています）と [distribution.xml](src/main/assembly/distribution.xml) でコンポーネント配置先を JDK 8 版 GraalVM のディレクトリ構造にフィットさせる必要があります
* [pom.xml](pom.xml) の `target.graalvm.version` をターゲットとする GraalVM のバージョンに置き換えれば、そこでも使えると思います

# ビルド

```
$ export JAVA_HOME=$GRAALVM_HOME
$ mvn package
```

`target` の下には以下の 2 つの JAR が生成されます。

* `truffle-nfi-wrapper.jar`
    * nfiwrapper 本体
* `truffle-nfi-wrapper-<バージョン>-component.jar`
    * `gu` でのインストールに用いるコンポーネント

# インストール

[gu](https://www.graalvm.org/docs/reference-manual/install-components/) を使ってビルドした `truffle-nfi-wrapper-<バージョン>-component.jar` をインストールします。

```
$ $GRAALVM_HOME/bin/gu -L install target/truffle-nfi-wrapper-0.1.0-component.jar

Processing Component archive: target/truffle-nfi-wrapper-0.1.0-component.jar
Installing new component: Truffle NFI Wrapper (nfiwrapper, version 0.1.0)
```

`gu list` で nfiwrapper が見えれば OK です。

```
$ $GRAALVM_HOME/bin/gu list
ComponentId              Version             Component name      Origin
--------------------------------------------------------------------------------
nfiwrapper               0.1.0               Truffle NFI Wrapper
graalvm                  20.1.0              GraalVM Core
```

# 試してみる

実態は NFI そのままなので、 [Truffle NFI のドキュメント](https://github.com/oracle/graal/blob/master/truffle/docs/NFI.md) にある、以下の流れでネイティブ関数を呼び出します。

1. ライブラリをロードする（ `load` 、または `default` ）
2. 関数シンボルからメンバを取得する
3. シグネチャをバインドする
4. 呼び出す

※シグネチャのフォーマットや C 言語の型とのマッピングについては [Truffle NFI のドキュメント](https://github.com/oracle/graal/blob/master/truffle/docs/NFI.md) に記載があります

例として libc の提供する [getpid()](https://linuxjm.osdn.jp/html/LDP_man-pages/man2/getpid.2.html) で自分自身の PID を取得してみます。今回は `load` を使ってライブラリをロードしていますが、libc は JVM 開始時にロードされるものであるため `default` でも問題なく動作します。

## Java の場合

[examples/GetPID.java](examples/GetPID.java) を実行してください。

```
$ $GRAALVM_HOME/bin/javac GetPID.java
$ $GRAALVM_HOME/bin/java GetPID
PID: 980
```

## JavaScript の場合

[polyglot](https://www.graalvm.org/docs/reference-manual/polyglot/#running-polyglot-applications) コマンドで実行できます。

```
$ $GRAALVM_HOME/bin/polyglot --shell
GraalVM MultiLanguage Shell 20.1.0
Copyright (c) 2013-2019, Oracle and/or its affiliates
  JavaScript version 20.1.0
  TruffleNFIWrapper version 0.1.0
Usage:
  Use Ctrl+L to switch language and Ctrl+D to exit.
  Enter -usage to get a list of available commands.
js> library = Polyglot.eval('nfiwrapper', 'load "libc.so.6"')
{}
js> getpid = library['getpid'].bind('():SINT32')
Native Symbol
js> getpid()
806
```
