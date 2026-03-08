.PHONY: test clean

CLANG := clang
CLANGXX := clang++
JAVA := $(JAVA_HOME)/bin/java
JAVAC := $(JAVA_HOME)/bin/javac

CXXFLAGS := $(shell llvm-config --cxxflags) -g -fPIC -std=c++20
LDFLAGS := $(shell llvm-config --ldflags)
LIBS := $(shell llvm-config --libs --system-libs) -lclang-cpp -lpthread

TARGET := libllvm-jit.so

SYSTEM_INCLUDE_PATHS := $(shell $(CLANG) -x c -v /dev/null -fsyntax-only 2>&1 | \
    sed -n '/search starts here:/,/End of search list./{ /search/d; /End of search/d; s/^[[:space:]]*//; s/.*/"&"/; p }' | \
    paste -sd, -)

CPPFLAGS := -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -DSYSTEM_INCLUDE_PATHS='$(SYSTEM_INCLUDE_PATHS)'


all:
	$(CLANGXX) llvm-jit.cpp $(CXXFLAGS) $(CPPFLAGS) $(LDFLAGS) $(LIBS) -shared -o $(TARGET)
	$(JAVAC) LLVMJit.java Test.java

test:
	$(JAVA) -Djava.library.path=. --enable-native-access=ALL-UNNAMED Test

clean:
	$(RM) $(TARGET) *.class
