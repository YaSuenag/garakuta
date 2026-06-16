#include <iostream>
#include <vector>
#include <execution>

#include "../common.h"


int main(int argc, char *argv[]){
  const int max = check_arg(argc, argv);
  std::vector<int> ary;
  ary.resize(max);
  std::iota(ary.begin(), ary.end(), 1);

  std::for_each(std::execution::par_unseq, ary.begin(), ary.end(), [](int &val){
    if(val % 3 == 0){
      val = 3;
    }
    else{
      do{
        if(val % 10 == 3){
          val = 3;
          break;
        }
        val /= 10;
      } while(val > 0);
    }
  });

  for(int idx = 0; idx < max; idx++){
    std::cout << (idx + 1);

    if(ary[idx] == 3){
      std::cout << " [aho]";
    }

    std::cout << std::endl;
  }

  return 0;
}
