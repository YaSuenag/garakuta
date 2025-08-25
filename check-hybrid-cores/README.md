ハイブリッドアーキテクチャの CPU コアをチェックする
===

Alder Lake 以降の Intel Core プロセッサに採用されたハイブリッドアーキテクチャ（P コアと E コア）のコア ID 割り当てをチェックします。

# ビルド

## Linux

```
gcc hy-core-check.c
```

## Windows

`cl.exe` を使えるようにしてください。Visual Studio 付属の Developer PowerShell が便利です。

```
cl hy-core-check.c
```

# 試してみる

## コマンド実行方法

### Linux

```
./a.out
```

### Windows

```
hy-core-check.exe
```

## 出力例

Linux でも Windows でも以下のような出力になります。

### ハイブリッドアーキテクチャの場合

```
Num of logical cores: 14
Hybrid: 1
HT: 1
Max leaf: 0x23
Core 0: type = 0x40, native model id = 0x2
Core 1: type = 0x40, native model id = 0x2
Core 2: type = 0x20, native model id = 0x2
Core 3: type = 0x20, native model id = 0x2
Core 4: type = 0x20, native model id = 0x2
Core 5: type = 0x20, native model id = 0x2
Core 6: type = 0x20, native model id = 0x2
Core 7: type = 0x20, native model id = 0x2
Core 8: type = 0x20, native model id = 0x2
Core 9: type = 0x20, native model id = 0x2
Core 10: type = 0x40, native model id = 0x2
Core 11: type = 0x40, native model id = 0x2
Core 12: type = 0x20, native model id = 0x2
Core 13: type = 0x20, native model id = 0x2
```

### ハイブリッドアーキテクチャでない場合

```
Num of logical cores: 14
Hybrid: 0
HT: 1
Max leaf: 0x1C
```

# カラクリ

* `Hybrid` で表示しているのは Leaf 07h の `EDX` の Bit 15 にある Hybrid フラグです
* `HT` で表示しているのは Leaf 01h の `EDX` の Bit 28 にある HTT フラグです
* コア情報は Leaf 1A の `EAX` を整形して出しています
    * Leaf 1A は `CPUID` を発行した論理プロセッサの情報を出すため、Windows では [SetThreadAffinityMask](https://learn.microsoft.com/ja-jp/windows/win32/api/winbase/nf-winbase-setthreadaffinitymask) 、Linux では [sched_setaffinity](https://man7.org/linux/man-pages/man2/sched_setaffinity.2.html) を使って実行コアを縛ってから `CPUID` を発行しています

# 参考情報

* Windows 環境では、ホスト環境ではハイブリッドコアの情報が取得できますが、仮想環境（Hyper-V ゲスト：WSL 2 含む）では情報が取得できません
    * `CPUID` の結果で Hybrid フラグが立っていません
* WSL 1 では Hybrid フラグも立ち Leaf 1A の情報も取得できますが、正確でない可能性が高いです
    * [この問題](https://github.com/microsoft/WSL/issues/3827) により `sched_setaffinity()` が機能していないようです

# 免責事項

Linux ではきちんとした動作確認はとっていません（ハイブリッドアーキテクチャのベアメタルマシンでのテストができていない）
