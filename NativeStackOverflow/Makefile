.PHONY: clean
.SUFFIXES: .java .class

JAVAC = $(JAVA_HOME)/bin/javac
AS = as
CC = gcc
ASFLAGS = -g -c --noexecstack

JAVA_CLASSES = NativeStackOverflow.class
OBJS = NativeOverflow.o
PROGRAM = liboverflow.so


all: $(PROGRAM) $(JAVA_CLASSES)

.s.o:
	$(AS) $(ASFLAGS) $< -o $@

$(PROGRAM): $(OBJS)
	$(CC) -shared -o $@ $^

.java.class:
	$(JAVAC) $<

clean:
	$(RM) $(PROGRAM) $(OBJS) $(JAVA_CLASSES)
