#!/bin/sh

ADD_MODULES='--add-modules jdk.internal.vm.ci'
EXPORT_MODULES='--add-exports jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED'
JAVA_OPTS="$ADD_MODULES $EXPORT_MODULES --add-opens jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI"

$JAVA_HOME/bin/javac $ADD_MODULES $EXPORT_MODULES jdk/vm/ci/code/test/TestAssembler.java jdk/vm/ci/code/test/TestHotSpotVMConfig.java jdk/vm/ci/code/test/amd64/AMD64TestAssembler.java A123.java
