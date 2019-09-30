.PHONY: clean
.SUFFIXES: .java .class

JAVA ?= $(JAVA_HOME)/bin/java
JAVAC ?= $(JAVA_HOME)/bin/javac

ADD_MODULES = --add-modules jdk.internal.vm.ci
EXPORT_MODULES = \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED \
  --add-exports jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED

JAVA_OPTS += $(ADD_MODULES) $(EXPORT_MODULES) \
             --add-opens jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED \
             -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI

# If you disassemble with hsdis, please enable here:
#JAVA_OPTS += -XX:+UnlockDiagnosticVMOptions \
#             -XX:CompilerDirectivesFile=printasm-directive.json

JAVA_CLASSES = helper/FuncLoader.class \
               helper/AsmInjector.class \
               helper/Util.class \
               CFuncTest.class

all: $(JAVA_CLASSES)

.java.class:
	$(JAVAC) $(ADD_MODULES) $(EXPORT_MODULES) $<

clean:
	find . -name "*.class" -exec $(RM) {} \;

test: CFuncTest.class
	$(JAVA) $(JAVA_OPTS) CFuncTest
