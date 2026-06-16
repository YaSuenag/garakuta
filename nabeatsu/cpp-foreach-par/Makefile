.PHONY: clean

PROGRAM = nabeatsu-cpp-foreach-par
OBJS = nabeatsu.o ../common.o

CXX = g++
CXXFLAGS = -g

all: test

test: $(PROGRAM)
	./$(PROGRAM) 100

$(PROGRAM): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^

.cpp.o:
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	$(RM) $(PROGRAM) $(OBJS)

