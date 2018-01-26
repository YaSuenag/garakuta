#!/bin/sh

COMMAND=$1

if [ -z "$COMMAND" ]; then
  echo 'run.sh [ compile | run | clean ]'
  exit -1
fi

ADD_MODULES='--add-modules jdk.internal.vm.ci'
EXPORT_MODULES='--add-exports jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED'
JAVA_OPTS="$ADD_MODULES $EXPORT_MODULES --add-opens jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI"

case $COMMAND in
  'compile')
    $JAVA_HOME/bin/javac $ADD_MODULES $EXPORT_MODULES jdk/vm/ci/code/test/TestAssembler.java jdk/vm/ci/code/test/TestHotSpotVMConfig.java jdk/vm/ci/code/test/amd64/AMD64TestAssembler.java A123.java;;
  'run')
    $JAVA_HOME/bin/java $JAVA_OPTS A123;;
  'clean')
    find . -name '*.class' -exec rm -f {} \; ;;
  *)
    echo 'Unknown command'
esac

