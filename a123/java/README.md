# 概要

Stackoverflowで [話題](https://stackoverflow.com/questions/48270127/can-a-1-a-2-a-3-ever-evaluate-to-true) だった、 `a == 1 && a == 2 && a == 3` を `true` にするJavaプログラムです。
Java 9から導入された [Java-Level JVM Compiler Interface](http://openjdk.java.net/jeps/243) （JVMCI）を使って、この比較演算を行うメソッドを任意の機械語に強制的に置き換えて実現しています。

JVMCIに関しては日本オラクルさんの [Java Day Tokyo 2017](http://www.oracle.co.jp/events/javaday/2017/) で詳しく説明させていただきました。なお、 [資料](https://www.slideshare.net/YaSuenag/panamajvmcijit) および [サンプルコード](https://github.com/YaSuenag/jdt-2017-examples) は公開しているので、興味がある方はぜひご覧ください。

# 試してみる

JDK 9以降を `JAVA_HOME` 環境変数にセットしておいてください。
[run.sh](run.sh) でコンパイル／実行すると `true` になります。

```
$ ./run.sh compile
$ ./run.sh run
true
```

# カラクリ

JVMCIを使って `A123#comp()` を強制的に任意の機械語に置き換えます。
生成する機械語では、 [AMD64向けSystem V ABI](https://software.intel.com/sites/default/files/article/402129/mpx-linux64-abi.pdf) で戻り値用のレジスタとして定義されている `RAX` に対して `1` を設定して、そのままリターンさせます。
なお、機械語生成にはOpenJDK 9のJVMCIテストコードである [TestAssembler.java](http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/TestAssembler.java) 、 [TestHotSpotVMConfig.java](http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/TestHotSpotVMConfig.java) 、 [AMD64TestAssembler.java](http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/amd64/AMD64TestAssembler.java) を流用しています。
JVMCIのクラスやメソッド、機械語生成方法に関する詳細については [Java Day Tokyo 2017発表資料](https://www.slideshare.net/YaSuenag/panamajvmcijit) および [サンプルコード](https://github.com/YaSuenag/jdt-2017-examples) をご覧ください。
