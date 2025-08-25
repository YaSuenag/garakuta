# 概要

[世界のナベアツ](https://ja.wikipedia.org/wiki/桂三度) さんのネタ「3の倍数と3のつく数字でアホになる」をプログラムにしたものです。

# 実装

* [OpenCL版](ocl)
    * OpenCL 3.0以降
* [OpenMP版](omp)
    * GCC 4.2以降
* [C++ AMP版](amp)
    * Visual Studio 2017以降
* [GraalVM版](graalvm)
    * GraalVM 19.3.0以降
    * 事前にllvm-toolchainをインストールしておいてください
        * `gu install llvm-toolchain`
* CUDA版
    * [nvcc](cuda/nvcc)
    * [Runtime Compilation](cuda/rtc)
    * [Runtime Compilation with Java Foreign Function & Memory API](cuda/java-rtc)

# 実行方法

## OpenCL版

```
$ cd ocl/build
$ cmake ..
$ cd ..
$ ./build/nabeatsu-cl [回数]
```

3rd Partyが提供するSDKなどを使用する場合は、それが提供するヘッダとライブラリをCMakeに認識させてください。 `OpenCL_ROOT` 環境変数にパスを指定することで認識させることができます。CUDA Toolkit が提供する OpenCL の場合は以下のように `cmake` を実行します。

```
$ OpenCL_ROOT=$CUDA_HOME cmake ..
```

> [!TIP]
> 2025 年 8 月現在、WSL 2 上の OpenCL はまだ WSL 側が対応していないようです https://github.com/microsoft/WSL/issues/6951  
> ただし、Intel Graphics の場合は WSL 2 で `nabeatsu-cl` をビルド／実行することが可能です。Fedora の場合は `intel-opencl`、`ocl-icd-devel` が必要です。

## OpenMP版

[omp](omp) で `make test` を実行してください。

## C++ AMP版

Visual Studio 2017以降で [amp/amp.sln](amp/amp.sln) を読み込んでビルドし、実行してください。

## GraalVM版

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

## CUDA版

### nvcc

```
$ cd cuda/nvcc/build
$ cmake -DCMAKE_PREFIX_PATH=$CUDA_HOME ..
$ ./nabeatsu-cl [回数]
```

### Runtime Compilation

[NVRTC](https://docs.nvidia.com/cuda/nvrtc/index.html) を使って、 [cuda/rtc/nabeatsu-rtc.c](cuda/rtc/nabeatsu-rtc.c) に `KERNEL_CODE` として定義されているカーネルコードを実行時にコンパイルして結果を得ます。

```bash
$ cd cuda/rtc
$ export CUDA_HOME
$ make
$ ./nabeatsu-rtc [回数]
```

Java 版の場合は JDK 22 をインストールし、コンパイルと実行をします。以下の例は WSL 2 Ubuntu 24.04 で CUDA 12.6 で動かしたものです。

```bash
$ cd cuda/java-rtc
$ export CUDA_HOME
$ export JAVA_HOME
$ mvn package
$ $JAVA_HOME/bin/java -Djava.library.path=$CUDA_HOME/lib64:/usr/lib/wsl/lib -jar target/nabeatsu-rtc-0.1.2.jar [回数]
```
