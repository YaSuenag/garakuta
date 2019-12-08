# 概要

[世界のナベアツ](https://ja.wikipedia.org/wiki/桂三度) さんのネタ「3の倍数と3のつく数字でアホになる」をプログラムにしたものです。

# 実装

* [OpenCL版](ocl)
    * OpenCL 2.0以降
    * [Beignet](https://www.freedesktop.org/wiki/Software/Beignet/) が提供する `libcl.so` に依存
    * FedoraかUbuntuかで [ocl/Makefile](ocl/Makefile) のリンカ設定を書き換えてください
* [OpenMP版](omp)
    * GCC 4.2以降
* [C++ AMP版](amp)
    * Visual Studio 2017以降
* [GraalVM版](graalvm)
    * GraalVM 19.3.0以降
    * 事前にllvm-toolchainをインストールしておいてください
        * `gu install llvm-toolchain`

# 実行方法

## OpenCL版、OpenMP版の場合

[ocl](ocl) 、または [omp](omp) で `make test` を実行してください。

## C++ AMP版の場合

Visual Studio 2017以降で [amp/amp.sln](amp/amp.sln) を読み込んでビルドし、実行してください。

## GraalVM版の場合

* GraalVM版は [graalvm/native/lib](graalvm/native/lib) にある `libnabeatsu.so` をGraalVMのLLVMツールチェーンを用いてビルドし、利用します
    * Javaからは `libnabeatsu.so` 内部の `.llvmbc` セクションにあるビットコードがGraalVMによって利用されます
    * 比較対象として、標準のC呼び出し（ELFバイナリ）を [graalvm/native](graalvm/native) に用意しています
    * 詳細は公式ブログを： https://medium.com/graalvm/graalvm-llvm-toolchain-f606f995bf
* [graalvm/run.sh](graalvm/run.sh) 経由で呼び出すことで、必要なもののビルドと実行が一気に可能です
* `$GRAALVM_HOME` を必ず設定してください
* バイナリを削除する場合は、各ディレクトリで `make clean` してください
    * [graalvm/java](graalvm/java)
    * [graalvm/native](graalvm/native)
    * [graalvm/native/lib](graalvm/native/lib)

### JavaからのPolyglot呼び出し

```
$ ./run.sh java [回数]
```

### ネイティブ実行

```
$ ./run.sh native [回数]
```
