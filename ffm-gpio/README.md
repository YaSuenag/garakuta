Foreign Function & Memory API で Memory Mapped I/O (MMIO) してみる
===================

Java の Foreign Function & Memory API (FFM) を使って Raspberry Pi の GPIO 5 に接続した LED を点滅させます。

# 動作確認環境

* Raspberry Pi 4
* Fedora 44
* JDK 26

# 動かしてみる

## Raspberry Pi と LED の接続

このプログラムは GPIO 5 の出力を ON/OFF するようハードコードされています。GPIO 5 を LED に接続してください。

## コンパイル

```
javac *.java
```

## 実行

```
java Blink
```

> [!NOTE]
> このプログラムは `/dev/mem` にアクセスします。Fedora のような一部 OS では `grubby --update-kernel=ALL --args="iomem=relaxed"` のようにカーネルパラメータを設定しておく必要があります。

# 処理の流れ

1. FFM を使って `/dev/mem` を `O_RDWR | O_SYNC` で `open()` する
2. 1. で取得した FD から、GPIO レジスタの物理アドレス（0xFE200000）を `mmap()` でマップし `MemorySegment` を得る
    * ここで取得した `MemorySegment` は GPIO レジスタがマップされた I/O 空間になるため、以後そこに `set()` することで GPIO の操作を行う

> [!NOTE]
> Java クラスライブラリにも `FileChannel::map` がありますが、これは（少なくとも JDK 28 までは）ファイルをマップサイズに合わせるよう `truncate(2)` を呼び出すため、デバイスファイルでは使えません。そのため `open(2)` と `mmap(2)` を自力で呼び出します。

3. `GPSEL0` の GPIO 5 を OUT に設定する
4. LED 点灯のため `GPSET0` で GPIO 5 を HIGH にしたあと 500ms スリープし、`GPCLR0` で GPIO 5 を LOW にし消灯する
5. FFM で `munmap(2)` と `close(2)` を呼び出し、リソースを解放する
