# 概要

[JVMCI](https://openjdk.java.net/jeps/243) を使って、JNIを経由せずに直接ライブラリ関数を呼び出すサンプルです。

JVMCIの詳細については [Java Day Tokyo 2017の資料](https://www.slideshare.net/YaSuenag/panamajvmcijit) 、および [サンプル](https://github.com/YaSuenag/jdt-2017-examples) をご覧ください。この資料との違いは、コールフレームに関する呼び出しオーバーヘッドを削減するために、JVMCIでターゲット関数へ直接ジャンプするようなコードになっているところです。

glibc（libc.so）のように `RTLD_DEFAULT` でルックアップできる関数が対象です。このサンプルでは例として `getpid` をJVMCI経由でロードします。

# 前提条件

* JDK 11以降
* Maven

# 試してみる

```
$ mvn package exec:exec
```

# 生成コードの確認

生成された機械語は、以下の2つの方法で確認することができます。

## 1. デバッガ

`jcmd <PID> Compiler.codelist` でインストールした機械語のアドレスを確認し、デバッガでディスアセンブルします。 `jcmd` を実行するタイミングは `mvn exec:exec` したコンソールで "Press any key to exit..." と表示されているときです。

以下、確認例です。  
PID 6069がテストコードで、そこのコードキャッシュの中から機械語のアドレス範囲を確認します。

```
$ jcmd
6069 com.yasuenag.garakuta.jvmci.jmp/com.yasuenag.garakuta.jvmci.jmp.CFuncTest
6021 org.codehaus.plexus.classworlds.launcher.Launcher exec:exec
6090 sun.tools.jcmd.JCmd
$ jcmd 6069 Compiler.codelist | grep yasuenag
289 4 0 com.yasuenag.garakuta.jvmci.jmp.helper.FuncLoader.getFuncAddr(J[B)J [0x00007f1c74732290, 0x00007f1c74732420 - 0x00007f1c74732438]
292 4 0 com.yasuenag.garakuta.jvmci.jmp.CFuncTest$GetPID.getPid()I [0x00007f1c74732490, 0x00007f1c74732620 - 0x00007f1c74732630]
```

例えば `getFuncAddr` の場合、アドレス範囲が 0x00007f1c74732420 から 0x00007f1c74732438 なので、ここをGDBでディスアセンブルします。

```
$ gdb -p 6069

    :

(gdb) disas 0x00007f1c74732420, 0x00007f1c74732438
Dump of assembler code from 0x7f1c74732420 to 0x7f1c74732438:
   0x00007f1c74732420:  mov    %rsi,%rdi
   0x00007f1c74732423:  lea    0x10(%rdx),%rsi
   0x00007f1c7473242a:  movabs $0x7f1c8c0ec4f0,%rax
   0x00007f1c74732434:  jmpq   *%rax
   0x00007f1c74732436:  hlt
   0x00007f1c74732437:  hlt
End of assembler dump.
```

## 2. hsdis

HotSpotのディスアセンブルライブラリであるhsdisを使う方法です。hsdisはOpenJDKのソースコードから自分でビルドすることをお奨めしますが、ビルド方法については [こちら](https://www.slideshare.net/YaSuenag/java-9-62345544/69) をご覧ください。

hsdisが組み込まれたJDKで `mvn exec:exec` する場合、引数に `-Ddisas=true` を追加することでインストールしたメソッドがディスアセンブルされます。

```
$ mvn exec:exec -Ddisas=true

    :

com/yasuenag/garakuta/jvmci/jmp/helper/FuncLoader.getFuncAddr(J[B)J (getFuncAddr)  [0x00007f1c307317a0, 0x00007f1c307317b8]  24 bytes
[Disassembling for mach='i386:x86-64']
[Entry Point]
[Verified Entry Point]
[Constants]
  # {method} {0x00007f1c19c02d08} 'getFuncAddr' '(J[B)J' in 'com/yasuenag/garakuta/jvmci/jmp/helper/FuncLoader'
  # parm0:    rsi:rsi   = long
  # parm1:    rdx:rdx   = '[B'
  #           [sp+0x10]  (sp of caller)
  0x00007f1c307317a0: mov    %rsi,%rdi
  0x00007f1c307317a3: lea    0x10(%rdx),%rsi
  0x00007f1c307317aa: movabs $0x7f1c4722c4f0,%rax
  0x00007f1c307317b4: jmpq   *%rax

    :

```
