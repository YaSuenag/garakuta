# 概要

Stackoverflowで [話題](https://stackoverflow.com/questions/48270127/can-a-1-a-2-a-3-ever-evaluate-to-true) だった、 `a == 1 && a == 2 && a == 3` を `true` にするC++プログラムです。
C++クラスの仮想関数テーブルをオーバーライドしています。

この方式は [HeapStats](https://github.com/HeapStats/heapstats) エージェントでもJava HotSpot VMでGCを実装しているクラスからJavaオブジェクトの情報を抜き取るために使用しています。 [a123.cpp](a123.cpp) はその実装の簡易版です。

# 試してみる

真になります。

```
$ g++ a123.cpp
$ ./a.out
1
```

# カラクリ

GCCがC++クラスからバイナリを生成する場合、 `virtual` 関数は `_ZTV` から始まる仮想関数テーブルを経由して `CALL` 命令を実行します。
そのため、仮想関数テーブルに入っている `A123::comp()` の関数ポインタを別のものにすり替えれば、それを実行することになります。

```
$ objdump -D a.out

    :

0000000000400a3e <main>:

    :

  400a98:       e8 cf 00 00 00          callq  400b6c <_ZN4A123C1Ev>  ;  ←コンストラクタ
  400a9d:       48 89 5d e8             mov    %rbx,-0x18(%rbp)
  400aa1:       48 8b 45 e8             mov    -0x18(%rbp),%rax
  400aa5:       48 8b 00                mov    (%rax),%rax
  400aa8:       48 8b 00                mov    (%rax),%rax
  400aab:       8b 4d dc                mov    -0x24(%rbp),%ecx
  400aae:       48 8b 55 e8             mov    -0x18(%rbp),%rdx
  400ab2:       89 ce                   mov    %ecx,%esi
  400ab4:       48 89 d7                mov    %rdx,%rdi
  400ab7:       ff d0                   callq  *%rax  ; ← comp() 呼び出し

    :

```

# 仮想関数オーバーライド時の注意

[a123.cpp](a123.cpp) のコメントにも記載していますが、最近のGCCではセキュリティ上の理由で仮想関数テーブルがメモリ上に展開されるときに、そのメモリ空間は書き込み権限（ `PROT_WRITE` ）が外されます。
そのままだと仮想関数テーブル上の関数ポインタを書き換える際にSEGVを起こしてクラッシュします。
それを避けるために [a123.cpp](a123.cpp) では `set_write_permission` で仮想関数テーブルが含まれるメモリ空間に `mprotect(2)` で `PROT_WRITE` を与えています。

# HeapStatsの場合は…

これは [HeapStats](https://github.com/HeapStats/heapstats) で使用している方式（仮想関数テーブルのオーバーライド）の簡易版だと説明しましたが、 [HeapStats](https://github.com/HeapStats/heapstats) ではHotSpot VM本体である `libjvm.so` から仮想関数テーブルや関数アドレスを取得するために `libbfd` を使って動的にシンボル探索を行っています。興味ある方は [HeapStats](https://github.com/HeapStats/heapstats) の [symbolFinder.cpp](https://github.com/HeapStats/heapstats/blob/master/agent/src/heapstats-engines/symbolFinder.cpp) や [vmFunctions.cpp](https://github.com/HeapStats/heapstats/blob/master/agent/src/heapstats-engines/vmFunctions.cpp) をぜひチェックしてみてください。
