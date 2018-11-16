.PHONY: clean
.SUFFIXES: .java .class

JAVAC = $(JAVA_HOME)/bin/javac
CC = gcc
CFLAGS = -g -fPIC
CPPFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

JAVA_CLASSES = OopInspectTest.class
OBJS = ooptest.o
PROGRAM = libooptest.so


all: $(PROGRAM) $(JAVA_CLASSES)

.c.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $< -o $@

$(PROGRAM): $(OBJS)
	$(CC) -shared $(CFLAGS) -o $@ $^

.java.class:
	$(JAVAC) $<

clean:
	$(RM) $(PROGRAM) $(OBJS) $(JAVA_CLASSES)
