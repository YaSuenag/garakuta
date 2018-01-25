# 概要

Stackoverflowで [話題](https://stackoverflow.com/questions/48270127/can-a-1-a-2-a-3-ever-evaluate-to-true) だった、 `a == 1 && a == 2 && a == 3` を `true` にするCプログラムです。
GCCの [診断機能](https://gcc.gnu.org/onlinedocs/gcc/Instrumentation-Options.html) を使っています。

# 試してみる

## 普通（偽になる）場合

```
$ gcc a123.c
$ ./a.out
0
```

## いじる（真になる）場合

```
$ gcc -finstrument-functions a123.c
$ ./a.out
1
```

# カラクリ

GCCで関数を抜けるときのフック `__cyg_profile_func_exit()` で、 `a == 1 && a == 2 && a == 3` の比較を行う `comp()` の戻りにのみフックをかけ、 `comp()` の戻りを保存しているレジスタをムリヤリ書き換えます。

## GCC 5.4 の生成するバイナリのディスアセンブル

```
$ objdump -d ./a.out

    :

00000000004005b6 <comp>:

    :

  4005fd:       e8 64 00 00 00          callq  400666 <__cyg_profile_func_exit>  ; ←関数exitのフック
  400602:       89 d8                   mov    %ebx,%eax  ; ←セーブした comp() 本来の戻り値の復活
  400604:       48 83 c4 18             add    $0x18,%rsp
  400608:       5b                      pop    %rbx
  400609:       5d                      pop    %rbp
  40060a:       c3                      retq

    :

```

上記のアセンブラが生成された場合、 `comp()` オリジナルの戻り値は `EBX` に格納されているため、 `__cyg_profile_func_exit()` の中で `EBX` に `1` を書き込みます。

# 注意

[AMD64向けSystem V ABI](https://software.intel.com/sites/default/files/article/402129/mpx-linux64-abi.pdf) では、 `EBX` レジスタは保存されるべきレジスタです。本来はこの値を呼び出し時オリジナルの値に復元することなく呼び出し元へ返るべきではありません。
