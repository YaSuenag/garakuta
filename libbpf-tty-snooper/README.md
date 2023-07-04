Linux カーネルの `tty_read()` を libbpf でフックしてカーネルモジュールで `printk()` する
===

# 必要なもの ※Fedoraの 場合

## 共通

* clang
* libbpf-devel
* kernel-devel
* kernel-debuginfo

`vmlinux` をカーネルモジュールの Makefile が参照できるようにする

```
sudo ln -s /usr/lib/debug/usr/lib/modules/`uname -r`/vmlinux /usr/src/kernels/`uname -r`/
```

## C の場合

* make

## Rust の場合

* cargo

# ビルド方法

## カーネルモジュール

```
cd kmod
make
```

`make` の結果、何もエラーがないことを確認する（`vmlinux` がカーネルモジュールの Makefile から参照できるところにないと警告メッセージが表示される）

## C

```
cd co-re/c
make
```

## Rust

```
cd co-re/rust
cargo build
```

# 使い方

## 1. カーネルモジュールをロードする

```
cd kmod
sudo insmod tty_snooper.ko
```

## 2. ユーザーランドプログラムを実行する

C も Rust も実行ファイル名や引数は一緒

```
sudo ./tty_snooper <TTY のでデバイスファイル>
```

## 3. ログを確認する

tty_snooper で指定した TTY に対してキー入力し、それが `dmesg` や `journalctl` で出力されていることを確認する
