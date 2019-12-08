.PHONY: clean

CXX ?= g++
CXXFLAGS += -g -O3

TARGET := nabeatsu
OBJS := nabeatsu-caller.o ../../common.o
LDFLAGS += -Llib -lnabeatsu

all: $(TARGET)

$(TARGET): $(OBJS)
	$(MAKE) -C lib
	$(CXX) -o $@ $^ $(LDFLAGS)

.cpp.o:
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	$(RM) $(OBJS) $(TARGET)
