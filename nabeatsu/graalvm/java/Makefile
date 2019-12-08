.PHONY: clean
.SUFFIXES: .java .class

JAVAC = $(GRAALVM_HOME)/bin/javac
JAVA_CLASSES = Nabeatsu.class

all: $(JAVA_CLASSES)
	$(MAKE) -C ../native/lib/

.java.class:
	$(JAVAC) $<

clean:
	$(RM) $(JAVA_CLASSES)
