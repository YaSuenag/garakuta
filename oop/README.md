# 概要

JNI関数に渡される `this` オブジェクトのOOP（Ordinary Object Pointer: HotSpot内で扱われるJavaオブジェクトのC++表現）のアドレスを出力します。ここで表示されるアドレスをHSDBのInspector（CLHSDBなら `inspect` ）に与えることで、OOPの中身を見ることができます。

# カラクリ

HotSpotはJNI仕様で [参照型となっているもの](https://docs.oracle.com/en/java/javase/11/docs/specs/jni/types.html#reference-types) はOOPへのポインタに変換してJNI関数へ渡されます。

単純なWide Oopの場合（Compressed Oopでない） `JNIHandles::make_local()` を通して `oop` のポインタ（ `oop*' ）がJNI関数へ渡されます。HotSpot内部のそれぞれの型との関係は以下のとおりです（※実際のHotSpotの実装とは違います）。

```
typedef     oop  oopDesc*
typedef jobject      oop*
```

※Compressed Oopの場合は `narrowOop` になり、 `oop` のアドレスがベースアドレスからのオフセットとビットシフトで計算されます。

なお、 [HeapStats](https://github.com/HeapStats/heapstats) はJavaオブジェクトの内容を取得するために、上記のようにOOPのポインタを直接たどることでJNI呼び出しのオーバーヘッドを削減しています。

# 試してみる

## テストプログラムのコンパイルと起動

```
$ export JAVA_HOME
$ make
$ java -Djava.library.path=. OopInspectTest
this object = 0x89a0add8
PID = 540
```

## 確認してみる

`jhsdb hsdb` でHSDB、または `jhsdb clhsdb` でCLHSDBを起動し、サンプルプログラムの `this object` で出力されているアドレスをInspectorに入力します。

すると、 `this` オブジェクト（ `OopInspectTest` ）のフィールドが参照でき、インスタンスフィールド `testField` に設定している文字列 `from main()` を確認することができます。
