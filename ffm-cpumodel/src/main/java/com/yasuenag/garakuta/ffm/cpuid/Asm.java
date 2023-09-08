package com.yasuenag.garakuta.ffm.cpuid;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.*;


public class Asm{

  private static final int RAX = 0;
  private static final int RBX = 3;
  private static final int RCX = 1;
  private static final int RDX = 2;
  private static final int RSP = 4;
  private static final int RBP = 5;
  private static final int RSI = 6;
  private static final int RDI = 7;
  private static final int R8 = 8;

  private final MemorySegment mem;

  private long tail;

  private MethodHandle hndCpuid;

  public Asm(long addr, long size){
    mem = MemorySegment.ofAddress(addr).reinterpret(size);
    tail = 0;

    generateCpuidCode();
  }

  private void emitPush(ByteBuffer buf, int reg){
    buf.put((byte)(0x50 | reg));
  }

  private void emitREXOp(ByteBuffer buf, boolean isREXW, int r, int m){
    byte rexw = isREXW ? (byte)0b1000 : (byte)0;
    byte rexr = (byte)(((r >> 3) << 2) & 0b0100);
    byte rexb = (byte)((m >> 3) & 0b0001);
    byte rex = (byte)(rexw | rexr | rexb);
    if(rex != 0){
      rex |= (byte)(0b01000000 | rex);
      buf.put(rex);
    }
  }

  private void emitMovRegToReg64RM(ByteBuffer buf, int r, int m){
    emitREXOp(buf, true, r, m);
    buf.put((byte)0x8b); // MOV
    buf.put((byte)(      0b11 << 6  | // REG-REG
                   ((r & 0x7) << 3) |
                    (m & 0x7)));
  }

  private void emitMovRegToReg64MR(ByteBuffer buf, int m, int r){
    emitREXOp(buf, true, r, m);
    buf.put((byte)0x89); // MOV
    buf.put((byte)(      0b11 << 6  | // REG-REG
                   ((r & 0x7) << 3) |
                    (m & 0x7)));
  }

  private void emitMovRegToMem32MR(ByteBuffer buf, int m, int r, byte offset){
    emitREXOp(buf, false, r, m);
    buf.put((byte)0x89); // MOV
    buf.put((byte)(      0b01 << 6  | // REG-MEM
                   ((r & 0x7) << 3) |
                    (m & 0x7)));
    buf.put(offset);
  }

  private void emitCpuid(ByteBuffer buf){
    buf.put((byte)0x0f);
    buf.put((byte)0xa2);
  }

  private void emitLeave(ByteBuffer buf){
    buf.put((byte)0xc9);
  }

  private void emitRet(ByteBuffer buf){
    buf.put((byte)0xc3); // near return
  }

  private void generateCpuidCode(){
    long start = tail;
    var memCpuid = mem.asSlice(tail);
    var buf = memCpuid.asByteBuffer().order(ByteOrder.nativeOrder());
    /*
       push %rbp
       mov %rsp, %rbp
       mov %rdi, %rax
       mov %rsi, %rcx
       mov %rdx, %r11
       cpuid
       movl %eax, (%r11)
       movl %ebx, 4(%r11)
       movl %ecx, 8(%r11)
       movl %edx, 12(%r11)
       leave
       ret
    */
    emitPush(buf, RBP);
    emitMovRegToReg64RM(buf, RBP, RSP);
    emitMovRegToReg64RM(buf, RAX, RDI);
    emitMovRegToReg64RM(buf, RCX, RSI);
    emitMovRegToReg64MR(buf, R8, RDX);
    emitCpuid(buf);
    emitMovRegToMem32MR(buf, R8, RAX, (byte)0);
    emitMovRegToMem32MR(buf, R8, RBX, (byte)4);
    emitMovRegToMem32MR(buf, R8, RCX, (byte)8);
    emitMovRegToMem32MR(buf, R8, RDX, (byte)12);
    emitLeave(buf);
    emitRet(buf);

    tail += buf.position();
    FunctionDescriptor desc = FunctionDescriptor.ofVoid(
                                ValueLayout.JAVA_INT, // eax
                                ValueLayout.JAVA_INT, // ecx
                                ValueLayout.ADDRESS // memory for eax - edx
                              );
    hndCpuid = Linker.nativeLinker().downcallHandle(memCpuid, desc);
  }

  public byte[] cpuid(int eax, int ecx) throws Throwable{
    try(var arena = Arena.ofConfined()){
      var result = arena.allocate(16, 1);
      hndCpuid.invoke(eax, ecx, result);
      return result.toArray(ValueLayout.JAVA_BYTE);
    }
  }

}
