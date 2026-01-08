.PHONY: clean
.SUFFIXES: .java .class

AS = as
LD = ld
ASFLAGS = -g -c --noexecstack
LDFLAGS = -shared -z noexecstack

TARGET  = libsegv.so
OBJS    = segv.o
CLASSES = SegvInFrameless.class

all: $(TARGET) $(CLASSES)

$(TARGET): $(OBJS)
	$(LD) $(LDFLAGS) -o $@ $(OBJS)

.s.o:
	$(AS) $(ASFLAGS) $< -o $@

.java.class:
	$(JAVA_HOME)/bin/javac $<

clean:
	$(RM) $(TARGET) $(OBJS) $(CLASSES)
