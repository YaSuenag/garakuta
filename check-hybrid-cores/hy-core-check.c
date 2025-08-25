#ifdef _WIN32
#include <Windows.h>
#else
#define _GNU_SOURCE
#include <sched.h>
#include <sys/sysinfo.h>
#endif

#include <stdio.h>

int get_logical_cores(){
  int result;

#ifdef _WIN32
  result = GetActiveProcessorCount(ALL_PROCESSOR_GROUPS);
#else
  result = get_nprocs();
#endif

  return result;
}

int get_max_leaf(){
  int result;

#ifdef _WIN32
  int regs[4];
  __cpuid(regs, 0);
  result = regs[0];
#else
  asm volatile(
    "xorl %%eax, %%eax\n"
    "cpuid"
    : "=a"(result) : : "ebx", "ecx", "edx"
  );
#endif

  return result;
}

int is_hybrid(){
  int edx;

#ifdef _WIN32
  int regs[4];
  __cpuidex(regs, 0x07, 0);
  edx = regs[3];
#else
  asm volatile(
    "movl $0x07, %%eax\n"
    "xorl %%ecx, %%ecx\n"
    "cpuid"
    : "=d"(edx) : : "eax", "ebx", "ecx"
  );
#endif

  return (edx >> 15) & 1;
}

int is_ht(){
  int edx;

#ifdef _WIN32
  int regs[4];
  __cpuid(regs, 0x01);
  edx = regs[3];
#else
  asm volatile(
    "movl $0x01, %%eax\n"
    "cpuid"
    : "=d"(edx) : : "eax", "ebx", "ecx"
  );
#endif

  return (edx >> 28) & 1;
}

void show_cpu_info(int cpus, int id){
  int eax;

#ifdef _WIN32
  HANDLE hThread = GetCurrentThread();
  SetThreadAffinityMask(hThread, 1 << id);
  int regs[4];
  __cpuidex(regs, 0x1A, 0);
  eax = regs[0];
#else
  cpu_set_t *cpuset = CPU_ALLOC(cpus);
  CPU_ZERO(cpuset);
  CPU_SET(id, cpuset);
  sched_setaffinity(0, cpus, cpuset);

  asm volatile(
    "movl $0x1A, %%eax\n"
    "xorl %%ecx, %%ecx\n"
    "cpuid"
    : "=a"(eax) : : "ebx", "ecx", "edx"
  );

  CPU_FREE(cpuset);
#endif

  printf("Core %d: ", id);
  if(eax == 0){
    printf("<not available>\n", id);
  }
  else{
    int core_type = eax >> 24;
    int native_model_id = eax & 0xFFFFFF;
    printf("type = 0x%x, native model id = 0x%x\n", core_type, native_model_id);
  }

}

int main(){
  int cpus = get_logical_cores();
  printf("Num of logical cores: %d\n", cpus);

  int hybrid = is_hybrid();
  printf("Hybrid: %d\n", hybrid);
  int ht = is_ht();
  printf("HT: %d\n", ht);

  int maxleaf = get_max_leaf();
  printf("Max leaf: 0x%X\n", maxleaf);

  if(hybrid && (maxleaf >= 0x1A)){
    for(int i = 0; i < cpus; i++){
      show_cpu_info(cpus, i);
    }
  }

  return 0;
}
