.PHONY: clean

TARGET = tty_snooper
KERNEL_DEVEL_ROOT = /usr/src/kernels/$(shell uname -r)
BPF_CODE_ROOT = ../../bpf
CC = clang
CFLAGS = -g -O2 -Wall
CPPFLAGS = -I$(KERNEL_DEVEL_ROOT)
LDFLAGS = -lbpf

all: tty_read.skel.h
	$(CC) $(CFLAGS) -c tty_snooper.c -o tty_snooper.o
	$(CC) $(CFLAGS) tty_snooper.o $(LDFLAGS) -o $(TARGET)

tty_read.skel.h: tty_read.bpf.o
	$(shell bpftool gen skeleton tty_read.bpf.o > tty_read.skel.h)

tty_read.bpf.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -target bpf -c $(BPF_CODE_ROOT)/tty_read.bpf.c -o tty_read.bpf.o

clean:
	$(RM) $(TARGET) *.o *.skel.h
