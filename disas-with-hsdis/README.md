# 概要

[ffmasm](https://github.com/YaSuenag/ffmasm) で動的生成した機械語を OpenJDK 付属のディスアセンブラ [hsdis](https://github.com/openjdk/jdk/tree/f04a6422469709d22bd92bf5d00655f741956efd/src/utils/hsdis) を使ってディスアセンブルし、標準出力に表示します。

> [!NOTE]
> 本コードは Capstone を static link した hsdis を使い、Linux AMD64 でで動作確認しています。

# 必要なもの

* AMD64 な OS
* hsdis
    * Linux 版なら [hsdis-builder](https://github.com/YaSuenag/hsdis-builder) で簡単にビルドできます
* Java 22 以降
* Maven

# 試してみる

## ビルド

```
mvn package
```

## 実行

```
mvn -Dhsdis=/path/to/hsdis-amd64.so exec:java
```

# カラクリ

HotSpot はディスアセンブルする際、hsdis の `decode_instructions_virtual()` を呼び出します。これに event や printf 関連パラメータに全部 `NULL` が渡されると Capstone 版（ [hsdis-capstone.c](https://github.com/openjdk/jdk/blob/f04a6422469709d22bd92bf5d00655f741956efd/src/utils/hsdis/capstone/hsdis-capstone.c) ）は引数に渡されたメモリ領域のディスアセンブル結果を `stdout` に出力します。
