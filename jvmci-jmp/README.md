# 概要

[JVMCI](https://openjdk.java.net/jeps/243) を使って、JNIを経由せずに直接ライブラリ関数を呼び出すサンプルです。

JVMCIの詳細については [Java Day Tokyo 2017の資料](https://www.slideshare.net/YaSuenag/panamajvmcijit) 、および [サンプル](https://github.com/YaSuenag/jdt-2017-examples) をご覧ください。この資料との違いは、コールフレームに関する呼び出しオーバーヘッドを削減するために、JVMCIでターゲット関数へ直接ジャンプするようなコードになっているところです。

# 前提条件

glibc（libc.so）のように `RTLD_DEFAULT` でルックアップできる関数が対象です。このサンプルでは例として `getpid` をJVMCI経由でロードします。

# 試してみる

```
$ make JAVA_HOME=/path/to/jdk13 test
```
