jlink したアプリに AOT を設定する
===
# 概要

Java 24 から導入された [JEP 483: Ahead-of-Time Class Loading & Linking](https://openjdk.org/jeps/483) を `jlink` したカスタムランタイムに仕込みます。

# 必要なもの

* Java 24
* Maven

# ビルド

`aot` プロファイルを有効にすると AOT キャッシュを生成し、ランチャースクリプト（ Unix 系 OS なら `/bin/aotjlink` 、 Windows ならそれに加えて `bin\\aotjlink.bat` ）に `-XX:AOTCache` を設定します。

```bash
mvn -Paot package
```

> [!NOTE]
> `-Paot` を削除すると AOT キャッシュを作らない、普通のカスタムランタイムが生成されます。

# カラクリ

アプリケーション（[Main.java](src/main/java/com/yasuenag/garakuta/aotjlink/Main.java)）はただの Hello World なので、これを実行したときのクラスロード状況をトレーニングしてキャッシュを作成します。

[pom.xml](pom.xml) の `package` フェーズで以下の順に処理します。

## 1. `jlink` の実行

[maven-jlink-plugin](https://maven.apache.org/plugins/maven-jlink-plugin/) を用いて `jlink` でカスタムランタイムを作成します。このときランチャーとして `aotjlink` を指定します。

2.以降は `aot` プロファイルが有効な時に実行されます。

## 2. クラスロードの記録

`-XX:AOTMode=record` を実行します。

アプリケーションはランチャー（ `aotjlink` ）経由で実行します。そのため `-XX:AOTMode=record -XX:AOTConfiguration=${project.build.directory}/app.aotconf` を `_JAVA_OPTIONS` 環境変数に設定することで HotSpot に AOT 関連設定を暗黙的に読み込ませます。

## 3. AOT キャッシュの作成

`-XX:AOTMode=create` を実行します。

AOT キャッシュの作成にアプリ実行は必要ないため、ランチャーではなく直接カスタムランタイム内の `java` コマンドを実行します。これはアプリケーションのモジュールがカスタムランタイムの `modules` に含まれるためです。 `java -XX:AOTMode=create -XX:AOTConfiguration=${project.build.directory}/app.aotconf -XX:AOTCache=app.aot -m ${mainModuleAndClass}` を実行します。

この実行は [exec-maven-plugin](https://www.mojohaus.org/exec-maven-plugin/) 経由で行いますが、 `<workingDirectory>` をカスタムランタイムの `bin` に設定しているため、AOT キャッシュはこのディレクトリに出力されます。

## 4. ランチャースクリプトの変更

AOT キャッシュを有効にして実行するためには `-XX:AOTCache=app.aot` を実行時に付与しなければなりません。ただし、アプリケーションの実行はランチャー経由になるため、ランチャー内で `-XX:AOTCache` を指定する必要があります。

もともと `jlink` が生成するランチャースクリプトは自身が配置されているディレクトリを `DIR` に設定し、さらにコマンドライン引数用に `JLINK_VM_OPTIONS` を空文字列で設定します。AOT キャッシュをロードさせるのにファイルの位置を正確に指定するため、この 2 つの変数を活用して `JLINK_VM_OPTIONS="-XX:AOTCache=$DIR/app.aot"` を設定します。ただし、以下の問題があるため [maven-antrun-plugin](https://maven.apache.org/plugins/maven-antrun-plugin/) で文字列の置換を実施します。

* `JLINK_VM_OPTIONS` を `jlink` 実行時に引数等で設定できない
* `DIR` の設定が `JLINK_VM_OPTIONS` の後にある
