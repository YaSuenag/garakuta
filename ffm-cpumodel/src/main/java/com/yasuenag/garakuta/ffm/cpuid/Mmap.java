package com.yasuenag.garakuta.ffm.cpuid;

import java.lang.foreign.*;
import java.lang.invoke.*;


public class Mmap{

  private static final SymbolLookup sym;

  private MethodHandle hndMmap;

  private MethodHandle hndMunmap;

  // #define PROT_READ       0x1             /* page can be read */
  public static final int PROT_READ = 0x1;

  // #define PROT_WRITE      0x2             /* page can be written */
  public static final int PROT_WRITE = 0x2;

  // #define PROT_EXEC       0x4             /* page can be executed */
  public static final int PROT_EXEC = 0x4;

  // #define MAP_PRIVATE     0x02            /* Changes are private */
  public static final int MAP_PRIVATE = 0x02;

  // #define MAP_ANONYMOUS   0x20            /* don't use a file */
  public static final int MAP_ANONYMOUS = 0x20;

  static{
    sym = Linker.nativeLinker().defaultLookup();
  }

  public Mmap(){
    hndMmap = null;
    hndMunmap = null;
  }

  public long mmap(long addr, long length, int prot, int flags, int fd, long offset) throws Throwable{
    if(hndMmap == null){
      MemorySegment func = sym.find("mmap").get();
      FunctionDescriptor desc = FunctionDescriptor.of(
                                  ValueLayout.ADDRESS, // return value
                                  ValueLayout.JAVA_LONG, // addr
                                  ValueLayout.JAVA_LONG, // length
                                  ValueLayout.JAVA_INT, // prot
                                  ValueLayout.JAVA_INT, // flags
                                  ValueLayout.JAVA_INT, // fd
                                  ValueLayout.JAVA_LONG // offset
                                );
      hndMmap = Linker.nativeLinker().downcallHandle(func, desc, Linker.Option.isTrivial());
    }

    MemorySegment mem = (MemorySegment)hndMmap.invoke(addr, length, prot, flags, fd, offset);
    long retval = mem.address();
    if(retval == -1){ // MAP_FAILED
      throw new RuntimeException("mmap() failed!");
    }
    return retval;
  }

  public int munmap(long addr, long length) throws Throwable{
    if(hndMunmap == null){
      MemorySegment func = sym.find("munmap").get();
      FunctionDescriptor desc = FunctionDescriptor.of(
                                  ValueLayout.JAVA_INT, // return value
                                  ValueLayout.JAVA_LONG, // addr
                                  ValueLayout.JAVA_LONG // length
                                );
      hndMunmap = Linker.nativeLinker().downcallHandle(func, desc, Linker.Option.isTrivial());
    }

    int result = (int)hndMunmap.invoke(addr, length);
    if(result == -1){
      throw new RuntimeException("munmap() failed!");
    }
    return result;
  }

}
