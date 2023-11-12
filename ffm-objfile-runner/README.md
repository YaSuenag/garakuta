# 概要

GCC の `-c` で得られるオブジェクトファイルを Foreign Function & Memory API を使って実行します。  
⚠️動的リンカが働かないため、 `printf()` など libc を含む外部ライブラリの呼び出しは機能しません。

# 必要なもの

* Java 21
* Maven
* GCC

# 使い方

```
$ mvn package
$ mvn exec:exec@run
```

呼び出し対象の C ソースは [src/main/native/test.c](src/main/native/test.c) です。呼び出す関数は [pom.xml](pom.xml) の以下の部分で変更可能です。

```xml
<execution>
    <id>run</id>
    <phase>exec</phase>
    <goals>
        <goal>exec</goal>
    </goals>
    <configuration>
        <executable>${java.home}/bin/java</executable>
        <workingDirectory>${project.build.directory}</workingDirectory>
        <arguments>
            <argument>-classpath</argument>
            <classpath/>
            <argument>--enable-preview</argument>
            <argument>--enable-native-access=ALL-UNNAMED</argument>
            <argument>${mainClass}</argument>
            <argument>${project.build.directory}/test.o</argument>

            <!-- ここで関数を指定する（mul2 か mul3） -->
            <argument>mul2</argument>
            <!-- <argument>mul3</argument> -->

        </arguments>
    </configuration>
</execution>
```

# カラクリ

`gcc -c` で得られたオブジェクトファイルを [FileChannel::map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/channels/FileChannel.html#map(java.nio.channels.FileChannel.MapMode,long,long)) で [MappedByteBuffer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/MappedByteBuffer.html) にマップします。そこから `MemorySegment` を `ofBuffer()` で取得します。

`FileChannel::map` には `MemorySegment` を返すものも存在しますが、ELF の解析に使う [JElf](https://github.com/fornwall/jelf) v0.9.0 の [ElfFile](https://javadoc.io/doc/net.fornwall/jelf/latest/net/fornwall/jelf/ElfFile.html) が `ByteBuffer` 系は `MappedByteBuffer` しか受け付けないため、これを経由して `MemorySegment` を取得するようにします。

これに FFM 経由で `mprotect()` で実行ビットを立て、実行可能メモリセグメントにします。ELF から取得した関数開始オフセットに対して `MethodHandle` を作成し、実行します。
