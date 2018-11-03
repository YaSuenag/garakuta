# 概要

ネイティブライブラリ内で意図的にSEGVを起こすJNIライブラリです。Javaのクラッシュ時の挙動の確認などにご利用ください。

# 試してみる

```
$ export JAVA_HOME
$ make
$ java -Djava.library.path=. NativeSEGV
```

