プロセッサコア、バス、TSC のクロックを調べてみる
===

# ビルド

## Linux

```
gcc freq.c
```

## Windows

`cl.exe` を使えるようにしてください。Visual Studio 付属の Developer PowerShell が便利です。

```
cl freq.c
```

# 試してみる

## コマンド実行方法

### Linux

```
./a.out
```

### Windows

```
freq.exe
```

## 出力例

Linux でも Windows でも以下のような出力になります。

```
max_leaf: 0x23

TSC frequency:
  924516352 Hz

CPU Frequency:
  Base: not available
  Max: not available
  Bus: not available
```

# カラクリ

* TSC の周波数は Leaf 15h の結果から計算しています
    * Core Ultra 5 225U だと仮想環境では出てきません（WSL 1 なら出てくる）
* CPU 周波数は Leaf 16h の結果から算出しています
    * Core ultra 5 225U だとホスト OS でも情報が出てきません
* Ryzen 3 3300X では 15h と 16h どちらの Leaf もサポートされていません

# 免責事項

CPU 周波数についてはきちんとした動作確認はとっていません（手持ちの PC で出てこなかったため）
