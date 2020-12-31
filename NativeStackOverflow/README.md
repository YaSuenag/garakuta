# 概要

ネイティブライブラリ内で意図的にネイティブレベルのスタックオーバーフローを起こすJNIライブラリです。hs_errが出ないクラッシュを起こします。

昔はこれでhs_errが吐けたのですが、LinuxカーネルかSolarisかの脆弱性の関係で `sigaltstack` を使ったエラーハンドリングがなくなったそうです。詳しい経緯は [OpenJDKのML](http://mail.openjdk.java.net/pipermail/hotspot-runtime-dev/2011-August/002354.html) をどうぞ。

# 対応環境

* Linux x86_64

# ネイティブレベルのスタックオーバーフローの起こし方

`alloca()` を使うとかいろいろありますが、このサンプルでは確実にスタックのガードページを触るために、アセンブラでプロローグ後にスタックポインタを上へ向かって順次書き込んでいきます。

# 試してみる

```
$ cd build
$ cmake ..
$ make
$ java -Djava.library.path=. -jar NativeStackOverflow.jar
```
