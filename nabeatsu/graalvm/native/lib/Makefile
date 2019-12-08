.PHONY: clean

TOOLCHAIN_PATH := $(shell $(GRAALVM_HOME)/bin/lli --print-toolchain-path)
CC := $(TOOLCHAIN_PATH)/clang
CFLAGS := -g -O3

TARGET := libnabeatsu.so
OBJS := nabeatsu.o

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) -shared -o $@ $^

.c.o:
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	$(RM) $(OBJS) $(TARGET)
