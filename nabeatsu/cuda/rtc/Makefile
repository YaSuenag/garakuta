.PHONY: clean

CC = gcc
CFLAGS = -g
CPPFLAGS = -I$(CUDA_HOME)/include
LDFLAGS = -L$(CUDA_HOME)/lib64 -L$(CUDA_HOME)/lib64/stubs -lcuda -lcudart -lnvrtc

OBJS = nabeatsu-rtc.o
PROGRAM = nabeatsu-rtc


all: $(PROGRAM)

.c.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $< -o $@

$(PROGRAM): $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	$(RM) $(PROGRAM) $(OBJS)
