#include <stdio.h>

void show_processor_frequency(){
  int base_freq, max_freq, bus_freq;
#ifdef _WIN32
  int regs[4];
  __cpuid(regs, 0x16);
  base_freq = regs[0];
  max_freq = regs[1];
  bus_freq = regs[2];
#else
  asm volatile("mov $0x16, %%eax\n"
               "cpuid"
               : "=a"(base_freq), "=b"(max_freq), "=c"(bus_freq) : : "edx"
  );
#endif

  printf("CPU Frequency:\n");
  printf("  Base: ");
  if(base_freq == 0){
    printf("not available\n");
  }
  else{
    printf("%d MHz\n", base_freq);
  }

  printf("  Max: ");
  if(max_freq == 0){
    printf("not available\n");
  }
  else{
    printf("%d MHz\n", max_freq);
  }

  printf("  Bus: ");
  if(bus_freq == 0){
    printf("not available\n");
  }
  else{
    printf("%d MHz\n", bus_freq);
  }
}

void show_tsc_frequency(){
  int nominal_clock, numerator, denominator;
#ifdef _WIN32
  int regs[4];
  __cpuid(regs, 0x15);
  denominator = regs[0];
  numerator = regs[1];
  nominal_clock = regs[2];
#else
  asm volatile("mov $0x15, %%eax\n"
               "cpuid"
               : "=a"(denominator), "=b"(numerator), "=c"(nominal_clock) : : "edx"
  );
#endif

  printf("TSC frequency:\n");
  if(numerator == 0 || nominal_clock == 0){
    printf("  not found\n");
  }
  else{
    printf("  %d Hz\n", nominal_clock * numerator / denominator);
  }
}

int main(){
  int max_leaf, nominal_clock, numerator, denominator;
#ifdef _WIN32
  int regs[4];
  __cpuid(regs, 0);
  max_leaf = regs[0];
#else
  asm volatile("xorl %%eax, %%eax\n"
               "cpuid"
               : "=a"(max_leaf) : : "ebx", "ecx", "edx"
  );
#endif

  printf("max_leaf: 0x%x\n", max_leaf);

  if(max_leaf >= 0x15){
    printf("\n");
    show_tsc_frequency();
  }
  if(max_leaf >= 0x16){
    printf("\n");
    show_processor_frequency();
  }

  return 0;
}
