package com.yasuenag.garakuta.ffm.objfile;

import java.lang.foreign.*;
import java.nio.channels.*;
import java.nio.file.*;

import net.fornwall.jelf.*;


public class RunObjFile{

  public static final int PROT_READ = 0x1;
  public static final int PROT_EXEC = 0x4;

  public static final long PAGE_SIZE = 4096;

  private static void setExecBit(MemorySegment seg) throws Throwable{
    var linker = Linker.nativeLinker();
    var mprotectSeg = linker.defaultLookup().find("mprotect").get();
    var mprotectDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT,  // return val
                                             ValueLayout.ADDRESS,   // addr
                                             ValueLayout.JAVA_LONG, // len
                                             ValueLayout.JAVA_INT); // prot
    var mprotect = linker.downcallHandle(mprotectSeg, mprotectDesc, Linker.Option.isTrivial());

    long alignmentMask = PAGE_SIZE - 1;
    long alignedSize = (seg.byteSize() + alignmentMask) & ~alignmentMask;
    mprotect.invoke(seg, alignedSize, PROT_READ | PROT_EXEC);
  }

  public static void main(String[] args) throws Throwable{
    String objFileName = args[0];
    String funcName = args[1];

    try(var chan = FileChannel.open(Path.of(objFileName), StandardOpenOption.READ)){
      var buf = chan.map(FileChannel.MapMode.READ_ONLY, 0, chan.size());
      var seg = MemorySegment.ofBuffer(buf);
      setExecBit(seg);

      var elf = ElfFile.from(buf);
      var sym = elf.getELFSymbol(funcName);
      var textSecHdr = elf.firstSectionByName(".text").header;
      var funcOffset = textSecHdr.sh_offset + sym.st_value;

      var desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
      var func = Linker.nativeLinker().downcallHandle(seg.asSlice(funcOffset), desc);

      int result = (int)func.invoke(100);
      System.out.println(STR."result: \{result}");
    }
  }

}
