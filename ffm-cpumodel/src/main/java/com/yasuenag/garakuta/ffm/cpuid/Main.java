package com.yasuenag.garakuta.ffm.cpuid;

import java.nio.*;
import java.nio.charset.*;


public class Main{

  private static final long ALLOCATE_SIZE = 4096;

  public static void main(String[] args) throws Throwable{
    var buf = ByteBuffer.allocate(4 * 4 * 3); // int * 4 registers * 3 ops
    Mmap mmap = new Mmap();
    long addr = mmap.mmap(0, ALLOCATE_SIZE, Mmap.PROT_EXEC | Mmap.PROT_READ | Mmap.PROT_WRITE, Mmap.MAP_PRIVATE | Mmap.MAP_ANONYMOUS, -1, 0);
    try{
      var asm = new Asm(addr, ALLOCATE_SIZE);
      buf.put(asm.cpuid(0x80000002, 0));
      buf.put(asm.cpuid(0x80000003, 0));
      buf.put(asm.cpuid(0x80000004, 0));
    }
    finally{
      mmap.munmap(addr, ALLOCATE_SIZE);
    }

    String str = new String(buf.array(), StandardCharsets.US_ASCII);
    System.out.println(str);
  }

}
