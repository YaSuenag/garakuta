ハイブリッドアーキテクチャの CPU コアをチェックする
===

Alder Lake 以降の Intel Core プロセッサに採用されたハイブリッドアーキテクチャ（P コアと E コア）のコア ID 割り当てをチェックします。ついでに CPU トポロジについても確認します。

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
Core 0: logical processor domain = 2, core domain = 14, type = 0x40, native model id = 0x2
Core 1: logical processor domain = 2, core domain = 14, type = 0x40, native model id = 0x2
Core 2: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 3: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 4: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 5: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 6: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 7: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 8: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 9: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 10: logical processor domain = 2, core domain = 14, type = 0x40, native model id = 0x2
Core 11: logical processor domain = 2, core domain = 14, type = 0x40, native model id = 0x2
Core 12: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
Core 13: logical processor domain = 2, core domain = 14, type = 0x20, native model id = 0x2
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
* 各項目は以下の値を整形して出しています
    * logical processor domain: Leaf 0Bh の `EBX` の Bit 15:0 （コアあたりの論理プロセッサ数）
    * core domain: Leaf 0Bh の `EBX` の Bit 15:0 （パッケージあたりの論理プロセッサ数）
    * tyoe: Leaf 1A の `EAX` の Bit 31:24 （コアタイプ）
    * native model id: Leaf 1A の `EAX` の Bit 23:0 （ネイティブモデル ID）
* Leaf 1A は `CPUID` を発行した論理プロセッサの情報を出すため、Windows では [SetThreadAffinityMask](https://learn.microsoft.com/ja-jp/windows/win32/api/winbase/nf-winbase-setthreadaffinitymask) 、Linux では [sched_setaffinity](https://man7.org/linux/man-pages/man2/sched_setaffinity.2.html) を使って実行コアを縛ってから `CPUID` を発行しています

# 参考情報

* Windows 環境では、ホスト環境ではハイブリッドコアの情報が取得できますが、仮想環境（Hyper-V ゲスト：WSL 2 含む）では情報が取得できません
    * `CPUID` の結果で Hybrid フラグが立っていません
* WSL 1 では Hybrid フラグも立ち Leaf 1A の情報も取得できますが、正確でない可能性が高いです
    * [この問題](https://github.com/microsoft/WSL/issues/3827) により `sched_setaffinity()` が機能していないようです

# 免責事項

Linux ではきちんとした動作確認はとっていません（ハイブリッドアーキテクチャのベアメタルマシンでのテストができていない）
