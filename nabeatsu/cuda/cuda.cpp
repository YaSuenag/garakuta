#include <iostream>

#include "../common.h"
#include "nabeatsu.cuh"

int main(int argc, char *argv[])
{
	int max = check_arg(argc, argv);
	int threads = getMaxThreadsPerBlock();
	int blocks = (max / threads) + 1;

	showInfo();

	std::cout << std::endl;

	bool *result = new bool[max];
	invokeNabeatsu(result, max, blocks, threads);

	for (int i = 0; i < max; i++) {
		std::cout << i + 1;
		if (result[i]) {
			std::cout << " [aho]";
		}
		std::cout << std::endl;
	}

	delete[] result;

	return 0;
}
