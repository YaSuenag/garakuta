# 概要

[世界のナベアツ](https://ja.wikipedia.org/wiki/桂三度) さんのネタ「3の倍数と3のつく数字でアホになる」をプログラムにしたものです。

# 実装

* [OpenCL版](ocl)
    * OpenCL 2.0以降
    * [Beignet](https://www.freedesktop.org/wiki/Software/Beignet/) が提供する `libcl.so` に依存
    * FedoraかUbuntuかで [ocl/Makefile](ocl/Makefile) のリンカ設定を書き換えてください
* [OpenMP版](omp)
    * GCC 4.2以降

# 実行方法

[ocl](ocl) 、または [omp](omp) で `make test` を実行してください。

