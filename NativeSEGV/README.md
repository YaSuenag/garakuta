# 概要

ネイティブライブラリ内で意図的にSEGVを起こすJNIライブラリです。Javaのクラッシュ時の挙動の確認などにご利用ください。

# 試してみる

```
$ export JAVA_HOME
$ cd build
$ cmake ..
$ make
$ java -Djava.library.path=. -jar NativeSEGV.jar
```

# Windowsの場合

`%JAVA_HOME%` 設定後、Visual StudioでNativeSEGVを開いてください。CMakeプロジェクトとして認識されます。Visual Studioでビルドすると `out\build` 配下にバイナリが出力されます。
