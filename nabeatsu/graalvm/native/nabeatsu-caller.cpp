#include <iostream>

#include "../../common.h"
#include "lib/nabeatsu.h"


int main(int argc, char *argv[]){
  int max = check_arg(argc, argv);

  for(int i = 1; i <= max; i++){
    std::cout << i;
    if(is_aho(i) == 1){
      std::cout << " [aho]";
    }
    std::cout << std::endl;
  }

  return 0;
}
