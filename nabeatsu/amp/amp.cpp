#include "stdafx.h"


int main(int argc, char *argv[])
{
	concurrency::accelerator_view acc = concurrency::accelerator::get_auto_selection_view();
	std::wcout << acc.accelerator.description << std::endl;

	int max = check_arg(argc, argv);
	int *array = new int[max];
	concurrency::array_view<int> view(max, array);

	concurrency::parallel_for_each(view.extent, [view](concurrency::index<1> idx) restrict(amp) {
		int val = idx[0] + 1;

		if (val % 3 == 0) {
			view[idx] = 3;
		}
		else {
			view[idx] = 0;

			do {

				if (val % 10 == 3) {
					view[idx] = 3;
				}

				val /= 10;
			} while (val > 0);

		}

	});

	for (int cnt = 0; cnt < max; cnt++) {
		std::cout << (cnt + 1);

		//if (array[cnt] == 3) {
		if (view[cnt] == 3) {
			std::cout << " [aho]";
		}

		std::cout << std::endl;
	}

	delete[] array;
    return 0;
}

