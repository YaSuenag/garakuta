Java の `String` で表現した C 言語ソースコードを LLVM で JIT コンパイルして FFM で実行する
===

[`clang` コマンドでビットコードを生成する](bc) バージョンと [コマンド実行せず Clang/LLVM API でコード生成する](cpp) バージョンの 2 種類があります。

# 必要なもの

* Linux AMD64
* Java 25 以降
* LLVM 21
    * 特に [API バージョン](cpp) はバージョンが異なるとコンパイルエラーになる可能性あり

# ビルド

## ビットコードバージョン

```
cd bc
javac *.java
```

## API バージョン

```
cd cpp
export JAVA_HOME
make
```

# 試してみる

各バージョンのディレクトリに含まれる Test.java では `C_SRC` に `printf` を実行する `hello` と `gettid()` を実行する `tid` の C 言語ソースコードが `String` で格納されています。これを動的にコンパイルして FFM 経由で実行することで結果を得られます。

## ビットコードバージョン

```
java --enable-native-access=ALL-UNNAMED Test
```

## API バージョン

```
make test
```

# カラクリ

## ビットコードバージョン

LLVM 呼び出しのキモは [bc/LLVMJit.java](bc/LLVMJit.java) にあります。

1. `static` イニシャライザで今回のサンプルを動かす最小限の関数群を `MethodHandle` にします
    * 関数は libLLVM.so から取得します
    * ライブラリのライフサイクルを `LLVMJit` と同期させたいので `SymbolLookup::libraryLookup` は使わず、`System::loadLibrary` でロードして `SymbolLookup::loaderLookup` で取得した `SymbolLookup` を使用して関数ポインタを取得します
2. LLVM の初期化で `LLVMInitializeNativeTarget()` と `LLVMInitializeNativeAsmPrinter()`、`LLVMInitializeNativeAsmParser()` の 3 つを呼ぶ必要がありますが、これらは LLVM の `Target.h` でプラットフォームごとに定義されたインライン関数であるため、AMD64 を前提としたマクロで定義されている関数を呼び出します
3. `clang -x c -emit-llvm -c -o - -` を呼び出し、このプロセスの stdin に C ソースを、stdout のビットコードを `byte[]` として取得します
4. 3. で取得したビットコードを `LLVMParseBitcode2` で LLVM Module（IR）にして、そこから `LLVMCreateJITCompilerForModule` で JIT のエンジンを作ります
    * LLVMJit.java の `compile()` では `optoLevel` で最適化レベルを指定可能です
5. 4. で作成したエンジンから実行したい関数のポインタを取得し、FFM の `Linker::downcallHandle` を使って `MethodHandle` にします
6. 5. で作成した `MethodHandle` を `invoke` します

## API バージョン

Clang と LLVM の C++ 呼び出しを JNI で実装することで、コマンド呼び出しをゼロにします。ただし、システムデフォルトのインクルードパスの設定には注意が必要です。

1. システムデフォルトのインクルードパスを `clang -x c -v /dev/null -fsyntax-only` で取得し、JNI 実装（ [llvm-jit.cpp](cpp/llvm-jit.cpp) ）コンパイル時にマクロ定数として（ `-D` ）渡します。
    * 出力結果を C の文字列配列として表現するための整形が必要です。具体的には [Makefile](cpp/Makefile) をご覧ください。
2. コンパイルしたい C ソースコードから `llvm::MemoryBuffer` を作成します
3. Clang フロントエンド（ `clang::CompilerInstance` ）のサーチパスに 1. で定義したパスを設定します
4. コンパイラフロントエンドを実行し IR を作成します
5. ORC JIT に最適化レベルを設定します
6. JIT コンパイルを実行し、4. で作った IR から機械語を生成します
7. 6. のコンパイル結果の関数ポインタを取得し、FFM で `MethodHandle` にします
8. 7. で作成板 `MethodHandle` を `invoke` します
9. 関数が不要になったら 6. で作ったインスタンスを `delete` します
