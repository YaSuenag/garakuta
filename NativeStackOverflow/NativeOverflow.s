.global Java_NativeStackOverflow_doOverflow
.type Java_NativeStackOverflow_doOverflow, @function

Java_NativeStackOverflow_doOverflow:
  push %rbp
  movq %rsp, %rbp
  xorq %rax, %rax

  1:
    movq %rax, (%rsp)
    subq $0x8, %rsp
    jmp 1b
