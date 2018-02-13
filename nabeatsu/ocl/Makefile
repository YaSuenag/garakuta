.PHONY: clean

PROGRAM = nabeatsu-cl
OBJS = nabeatsu.o ../common.o

CXX = g++
CXXFLAGS = -g -std=c++11

# Fedora
LDPATH = /usr/lib64/beignet
LDFLAGS = -L$(LDPATH) -lcl

#Ubuntu
#LDFLAGS = -lOpenCL

all: test

test: $(PROGRAM)
# Fedora
	LD_LIBRARY_PATH=$(LDPATH) ./$(PROGRAM) 100

# Ubuntu
#	./$(PROGRAM) 100

$(PROGRAM): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

.cpp.o:
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	$(RM) $(PROGRAM) $(OBJS)

