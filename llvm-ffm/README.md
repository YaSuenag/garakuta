Java の `String` で表現した C 言語ソースコードを LLVM で JIT コンパイルして FFM で実行する
===

# 必要なもの

* Linux AMD64
* Java 25 以降
* LLVM

# ビルド

```
javac *.java
```

# 試してみる

[Test.java](Test.java) では `C_SRC` に `printf` を実行する `hello` と `gettid()` を実行する `tid` の C 言語ソースコードが `String` で格納されています。以下のコマンドでその実行結果が表示されます。

```
java --enable-native-access=ALL-UNNAMED Test
```

# カラクリ

LLVM 呼び出しのキモは [LLVMJit.java](LLVMJit.java) にあります。

1. `static` イニシャライザで今回のサンプルを動かす最小限の関数群を `MethodHandle` にします
    * 関数は libLLVM.so から取得します
    * ライブラリのライフサイクルを `LLVMJit` と同期させたいので `SymbolLookup::libraryLookup` は使わず、`System::loadLibrary` でロードして `SymbolLookup::loaderLookup` で取得した `SymbolLookup` を使用して関数ポインタを取得します
2. LLVM の初期化で `LLVMInitializeNativeTarget()` と `LLVMInitializeNativeAsmPrinter()`、`LLVMInitializeNativeAsmParser()` の 3 つを呼ぶ必要がありますが、これらは LLVM の `Target.h` でプラットフォームごとに定義されたインライン関数であるため、AMD64 を前提としたマクロで定義されている関数を呼び出します
3. `clang -x c -emit-llvm -c -o - -` を呼び出し、このプロセスの stdin に C ソースを、stdout のビットコードを `byte[]` として取得します
4. 3. で取得したビットコードを `LLVMParseBitcode2` で LLVM Module（IR）にして、そこから `LLVMCreateJITCompilerForModule` で JIT のエンジンを作ります
    * LLVMJit.java の `compile()` では `optoLevel` で最適化レベルを指定可能です
5. 4. で作成したエンジンから実行したい関数のポインタを取得し、FFM の `Linker::downcallHandle` を使って `MethodHandle` にします
6. 5. で作成した `MethodHandle` を `invoke` します
