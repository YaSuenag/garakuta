#include <iostream>
#include <cstddef>

#include <unistd.h>
#include <sys/mman.h>


extern "C" void *_ZTV4A123;
extern "C" void *_ZN4A1234compEi;


class A123{
  public:
    virtual bool comp(int a){ return (a == 1) && (a == 2) && (a == 3); }
};


static bool return_true(A123 *thisptr, int a){
  return true;
}

/* http://gcc.gnu.org/ml/gcc-patches/2012-11/txt00001.txt */
static void set_write_permission(void *ptr){
  long page_sz = sysconf(_SC_PAGESIZE);
  ptrdiff_t start_page = (ptrdiff_t)ptr & ~(page_sz - 1);
  ptrdiff_t end_page = ((ptrdiff_t)ptr + sizeof(void *)) & ~(page_sz - 1);
  size_t len = page_sz + (end_page - start_page);
  mprotect((void *)start_page, len, PROT_READ | PROT_WRITE | PROT_EXEC);
}

int main(int argc, char *argv[]){
  void **vtable = &_ZTV4A123;
  while(*vtable != &_ZN4A1234compEi){
    vtable++;
  }
  set_write_permission(&_ZTV4A123);
  *vtable = (void *)&return_true;

  A123 *test = new A123();
  std::cout << test->comp(argc) << std::endl;
  delete test;

  return 0;
}
