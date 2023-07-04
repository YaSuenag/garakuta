.PHONY: clean

obj-m += tty_snooper.o

all: modules

modules:
	$(MAKE) -C /lib/modules/${shell uname -r}/build M=${shell pwd} modules

clean:
	$(MAKE) -C /lib/modules/${shell uname -r}/build M=${shell pwd} clean
