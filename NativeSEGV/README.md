# 概要

ネイティブライブラリ内で意図的にSEGVを起こすJNIライブラリです。Javaのクラッシュ時の挙動の確認などにご利用ください。

# 試してみる

```
$ gcc -shared -fPIC -o libsegv.so -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -lpthread NativeSEGV.c
$ javac NativeSEGV.java
$ java -Djava.library.path=. NativeSEGV
```

