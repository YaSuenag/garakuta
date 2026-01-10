# ガラクタ置き場

とめどない個人的なガラクタの置き場所

# おしながき

* `a == 1 && a == 2 && a == 3` を `true` にする
    * [C言語版](a123/c/)
    * [C++版](a123/cpp/)
    * [Java (JVMCI) 版](a123/java/)
* [AVX2命令を使った配列要素の足し算](simd-sum.c)
* [クラスに対するCleanerの設定](class_unload/) ※要Java 9
* [わざとSEGVを起こすJNIライブラリ](NativeSEGV/)
* [わざとネイティブレベルのスタックオーバーフローを起こすJNIライブラリ](NativeStackOverflow/)
* [世界のナベアツのネタをOpenCLとOpenMPとC++ AMPとGraalVMとCUDAでやってみる](nabeatsu)
* [WSL 上の git を Windows で使う](wslgit)
* [JNIのjobjectのOOPアドレスを取得する](oop)
* [JVMCIでライブラリ関数を呼んでみる](jvmci-jmp)
* [かんたんなJava用デッドロック検知器](java-deadlock-detector)
* [PodmanでElasticsearchとKibanaを簡単に動かす](ek-deployment)
* [JFRの記録を時間指定で抜き出す](trjfr)
* [Foreign Function & Memory APIでハンドアセンブルしたコードを実行する](ffm-cpumodel)
* [libbpf からカーネルモジュールを呼び出してみる](libbpf-tty-snooper)
* [GCC のオブジェクトファイルを FFM で実行してみる](ffm-objfile-runner)
* [乱数ジェネレータ性能比較 in Java](randbench)
* [動的生成コード（機械語）を Java から hsdis でディスアセンブルする](disas-with-hsdis)
* [FFM で仮想メソッドに upcall する](ffm-upcall-to-virtualmethod)
* [jlink したアプリに AOT を設定する](aot-jlink)
* [ハイブリッドアーキテクチャの CPU コアをチェックする](check-hybrid-cores)
* [Attach API のシグナルハンドラチェックをキャンセルする JVMTI エージェント](sigforce)
* [プロセッサコア、バス、TSC のクロックを調べてみる](freq)
* [Java の `Math::min` / `Math::max` を `double` で叩きまくるベンチマーク](randminmax)
