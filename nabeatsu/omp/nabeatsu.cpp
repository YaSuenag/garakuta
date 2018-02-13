#include <iostream>
#include <cstdlib>
#include <climits>

#include <omp.h>

#include "../common.h"


static void fill_3(int *vals, size_t len){
#pragma omp parallel for
  for(int idx = 0; idx < len; idx++){
    int val = idx + 1;

    if(val % 3 == 0){
      vals[idx] = 3;
    }
    else{
      vals[idx] = 0;

      do{

        if(val % 10 == 3){
          vals[idx] = 3;
          break;
        }

        val /= 10;
      } while(val > 0);

    }

  }
}

int main(int argc, char *argv[]){
  int max = check_arg(argc, argv);
  int *array = new int[max];

  fill_3(array, max);

  for(int idx = 0; idx < max; idx++){
    std::cout << (idx + 1);

    if(array[idx] == 3){
      std::cout << " [aho]";
    }

    std::cout << std::endl;
  }

  delete[] array;
  return 0;
}
