#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <climits>


int check_arg(int argc, char *argv[]){
  int ret;
  char *ptr;

  if(argc != 2){
    std::cerr << "baka" << std::endl;
    _Exit(3);
  }

  ret = (int)strtol(argv[1], &ptr, 10);
  if(*ptr != '\0'){
    std::cerr << "baka" << std::endl;
    _Exit(3);
  }
  else if(ret <= 0){
    std::cerr << "baka" << std::endl;
    _Exit(3);
  }
  else if((ret == LONG_MIN) || (ret == LONG_MAX)){
    perror("baka");
    _Exit(3);
  }

  return ret;
}

