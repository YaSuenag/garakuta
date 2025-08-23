.PHONY: clean

CXX ?= g++
CXXFLAGS += -g -shared -fPIC
CPPFLAGS += -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
LDFLAGS += -lpthread
TARGET := libsigforce.so

ELF_FORMAT = $(shell objdump -f $$JAVA_HOME/bin/java | grep 'file format' | sed -e 's/^.\+file format \(.\+\)$$/\1/')
ELF_ARCH = $(shell objdump -f $$JAVA_HOME/bin/java | grep 'architecture: ' | sed -e 's/^architecture: \(.\+\), .\+$$/\1/')


all: $(TARGET)

$(TARGET): sigforce.o java_transformer.o
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

sigforce.o: sigforce.cpp
	$(CXX) $(CXXFLAGS) $(CPPFLAGS) -c $< -o $@

java_transformer.o: com/yasuenag/garakuta/sigforce/Transformer.java
	$(JAVA_HOME)/bin/javac $<
	objcopy -I binary -O $(ELF_FORMAT) -B $(ELF_ARCH) com/yasuenag/garakuta/sigforce/Transformer.class $@

clean:
	$(RM) $(TARGET) *.o com/yasuenag/garakuta/sigforce/Transformer.class

