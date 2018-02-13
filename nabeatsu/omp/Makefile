.PHONY: clean

PROGRAM = nabeatsu-omp
OBJS = nabeatsu.o ../common.o

CXX = g++
CXXFLAGS = -g -fopenmp

all: test

test: $(PROGRAM)
	OMP_DISPLAY_ENV=TRUE GOMP_DEBUG=1 ./$(PROGRAM) 100

$(PROGRAM): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^

.cpp.o:
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	$(RM) $(PROGRAM) $(OBJS)

