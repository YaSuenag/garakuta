#include <stdio.h>

#define ARRAY_LEN 1000000


main(){
  int array[ARRAY_LEN];
  int idx;
  int result;

  for(idx = 0; idx < ARRAY_LEN; idx++){
    array[idx] = idx;
  }

/***************************************/
  result = 0;
  for(idx = 0; idx < ARRAY_LEN; idx++){
    result += array[idx];
  }

  printf("Normal loop: %d\n", result);
/***************************************/
  {
    int mask_array[9][8] = {
                { 0,  0,  0,  0,  0,  0,  0,  0}, /* dummy (should not use) */
                {-1,  0,  0,  0,  0,  0,  0,  0},
                {-1, -1,  0,  0,  0,  0,  0,  0},
                {-1, -1, -1,  0,  0,  0,  0,  0},
                {-1, -1, -1, -1,  0,  0,  0,  0},
                {-1, -1, -1, -1, -1,  0,  0,  0},
                {-1, -1, -1, -1, -1, -1,  0,  0},
                {-1, -1, -1, -1, -1, -1, -1,  0},
                {-1, -1, -1, -1, -1, -1, -1, -1}
    };

    asm volatile("movq    %1,  %%r8  # array length \n"
                 "shlq    $2,  %%r8  # convert array length to byte length \n"
                 "xor  %%rax, %%rax  # clear index \n"
                 "vpxor %%ymm0, %%ymm0, %%ymm0  \n"
                 "cmp    $32,  %%r8  \n"
                 "jl     .FRACTION   # jump normal loop if array size < 64 \n"

                 "movq    %%r8, %%r9  \n"
                 "andq $0xffffffffffffffe0, %%r9  # unroll limit bytes \n"
                 ".UNROLLED_LOOP:  # integer parallel add \n"
                 "  vpaddd (%2, %%rax), %%ymm0, %%ymm0  \n"
                 "  lea  32(%%rax), %%rax  \n"
                 "  cmpq      %%r9, %%rax  \n"
                 "  jl .UNROLLED_LOOP      \n"
                 "cmpq      %%r8, %%rax  \n"
                 "je       .EXIT  \n"

                 ".FRACTION:  \n"
                 "  subq %%rax, %%r8  \n"
                 "  shlq    $3, %%r8  # calculate mask index  \n"
                 "  vmovdqu    (%3, %%r8),  %%ymm1  # move mask  \n"
                 "  vpmaskmovd (%2, %%rax), %%ymm1, %%ymm2  \n"
                 "  vpaddd %%ymm2, %%ymm0, %%ymm0  \n"

                 ".EXIT:  \n"
                 "  vphaddd %%ymm0, %%ymm0, %%ymm0  \n"
                 "  vpermpd $0b00001000, %%ymm0, %%ymm0  # shuffle elements \n"
                 "  vphaddd %%ymm0, %%ymm0, %%ymm0  \n"
                 "  vphaddd %%ymm0, %%ymm0, %%ymm0  \n"
                 "  vmovd   %%xmm0, %0  # store result \n"
                : "=g"(result)
                : "i"(ARRAY_LEN), "r"(array), "r"(mask_array)
                : "cc", "memory", "%rax", "%r8", "%r9",
                                      "%xmm0", "%xmm1", "%xmm2");
    printf("  SIMD loop: %d\n", result);
  }

}

