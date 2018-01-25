#include <stdio.h>


int comp(int a){
  return (a == 1) && (a == 2) && (a == 3);
}

int main(int argc, char *argv[]){
  printf("%d\n", comp(argc));
  return 0;
}


__attribute__((no_instrument_function))
void __cyg_profile_func_exit(void *current_func, void *callsite){
  if(current_func == &comp){
    asm volatile("mov $1, %rbx");
  }
}

