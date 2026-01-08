.global Java_SegvInFrameless_doSEGV
.type Java_SegvInFrameless_doSEGV, @function
Java_SegvInFrameless_doSEGV:
.cfi_startproc
  xorq %rax, %rax
  mov  %rax, (%rax)
.cfi_endproc
.size Java_SegvInFrameless_doSEGV, .-Java_SegvInFrameless_doSEGV
