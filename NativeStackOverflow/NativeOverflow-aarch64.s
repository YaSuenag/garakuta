.global Java_NativeStackOverflow_doOverflow
.type Java_NativeStackOverflow_doOverflow, @function

Java_NativeStackOverflow_doOverflow:
  stp x29, x30, [sp, #-16]
  mov x29, sp

  1:
    stp xzr, xzr, [sp, #-16]!
    b 1b
