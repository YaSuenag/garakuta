FFM で仮想メソッドに upcall する
===
# 概要

FFM で upcall する対象を仮想メソッド（ `invokevirtual` や `invokeinterface` バイトコード命令で呼び出すもの = `static` や `private` ではないもの）にします。

# 必要なもの

* Java 23
* Maven
* GCC

# 使い方

```bash
mvn package
mvn exec:exec@run
```

# カラクリ

upcall に使用する `MethodHandle` の先頭に、その呼び出しで `this` インスタンスとなるものを `MethodHandles::insertArguments` で追加することで仮想関数の呼び出しを実現します。

[MethodHandles.Lookup::findVirtual の Javadoc](https://docs.oracle.com/javase/jp/23/docs/api/java.base/java/lang/invoke/MethodHandles.Lookup.html#findVirtual(java.lang.Class,java.lang.String,java.lang.invoke.MethodType)) にあるとおり、仮想メソッドを指す `MethodHandle` の第一引数はレシーバ（そのメソッドを受け取るインスタンス：そのメソッドを呼び出した際に `this` インスタンスとなるもの）を指定しなければなりません。一般的な upcall ではネイティブレイヤから渡された引数はすべてそのまま（論理的に）同じ順番で Java レイヤに渡されます（Java ではネイティブと呼び出し規約が違ったり暗黙的な引数が発生しているため、引数のシャッフル等が動的生成される FFM のスタブコードによって行われます）。FFM は Java のプリミティブ型とその配列型しか扱うことができないため、downcall 時に `this` インスタンスを渡してそれを upcall で渡すということができません。そのため、upcall する前に upcall メソッドの `MethodHandle` の先頭にレシーバとなる `this` インスタンスを `MethodHandles::insertArguments` で登録しておくのです。

このサンプルである [Main.java](src/main/java/com/yasuenag/garakuta/ffm/upcall/Main.java) では `native()` （ [native.c](src/main/c/native.c) で定義）やコールバックメソッドへのオリジナルのメソッドハンドル（ `MethodHandles.Lookup::findVirtual` で取得した、そのままのもの）は不変なものであるため、 `static` イニシャライザで初期化しています。ただし、実際に関数ポインタとして `native()` に渡す upcall 用 `MemorySegment` に使用する `MethodHandle` には前述のとおり `this` インスタンスを事前に挿入しておかなければならないため、その作業を `Main` のコンストラクタで実施しています。
