import java.lang.foreign.*;
import java.lang.invoke.*;


// All of methods should have synchronized keyword because they share errnoSeg.
public class Syscall{

  public static final int O_RDWR = 00000002;
  public static final int O_SYNC = 04000000 | 00010000; // (__O_SYNC|O_DSYNC)

  public static final int PROT_READ  = 0x1;
  public static final int PROT_WRITE = 0x2;
  public static final int MAP_SHARED = 0x1;

  private static final MethodHandle hndOpen;
  private static final MethodHandle hndClose;
  private static final MethodHandle hndMmap;
  private static final MethodHandle hndMunmap;

  private static final Linker.Option errnoState;
  private static final MemorySegment errnoSeg;
  private static final VarHandle hndErrno;

  static{
    var linker = Linker.nativeLinker();

    errnoState = Linker.Option.captureCallState("errno");
    errnoSeg = Arena.global().allocate(Linker.Option.captureStateLayout());
    hndErrno = Linker.Option.captureStateLayout().varHandle(MemoryLayout.PathElement.groupElement("errno"));

    var openDesc = FunctionDescriptor.of(
      ValueLayout.JAVA_INT, // return value
      ValueLayout.ADDRESS,  // path
      ValueLayout.JAVA_INT  // flags
    );
    var ptrOpen = linker.defaultLookup().findOrThrow("open");
    hndOpen = linker.downcallHandle(ptrOpen, openDesc, Linker.Option.firstVariadicArg(2), errnoState)
                    .bindTo(errnoSeg);

    var closeDesc = FunctionDescriptor.of(
      ValueLayout.JAVA_INT, // return value
      ValueLayout.JAVA_INT  // fd
    );
    var ptrClose = linker.defaultLookup().findOrThrow("close");
    hndClose = linker.downcallHandle(ptrClose, closeDesc, errnoState)
                     .bindTo(errnoSeg);

    var mmapDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS,                     // return value
      ValueLayout.ADDRESS,                     // addr
      linker.canonicalLayouts().get("size_t"), // length
      ValueLayout.JAVA_INT,                    // prot
      ValueLayout.JAVA_INT,                    // flags
      ValueLayout.JAVA_INT,                    // fd
      ValueLayout.JAVA_LONG                    // offset
    );
    var ptrMmap = linker.defaultLookup().findOrThrow("mmap");
    hndMmap = linker.downcallHandle(ptrMmap, mmapDesc, errnoState)
                    .bindTo(errnoSeg);

    var munmapDesc = FunctionDescriptor.of(
      ValueLayout.JAVA_INT,                   // return value
      ValueLayout.ADDRESS,                    // addr
      linker.canonicalLayouts().get("size_t") // length
    );
    var ptrMunmap = linker.defaultLookup().findOrThrow("munmap");
    hndMunmap = linker.downcallHandle(ptrMunmap, munmapDesc, errnoState)
                      .bindTo(errnoSeg);
  }

  public static synchronized int open(String path, int flags){
    try(var arena = Arena.ofConfined()){
      var ptrPath = arena.allocateFrom(path);
      int fd = (int)hndOpen.invoke(ptrPath, flags);
      if(fd == -1){
        throw new RuntimeException("open() failed (errno=" + (int)hndErrno.get(errnoSeg, 0L) + ")");
      }
      return fd;
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static synchronized int close(int fd){
    try{
      int retval = (int)hndClose.invoke(fd);
      if(retval == -1){
        throw new RuntimeException("close() failed (errno=" + (int)hndErrno.get(errnoSeg, 0L) + ")");
      }
      return retval;
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static synchronized MemorySegment mmap(MemorySegment addr, long length, int prot, int flags, int fd, long offset){
    try{
      MemorySegment mem = (MemorySegment)hndMmap.invoke(addr, length, prot, flags, fd, offset);
      if(mem.address() == -1L){ // MAP_FAILED
        throw new RuntimeException("mmap() failed (errno=" + (int)hndErrno.get(errnoSeg, 0L) + ")");
      }
      return mem.reinterpret(length);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static synchronized int munmap(MemorySegment addr, long length){
    try{
      int retval = (int)hndMunmap.invoke(addr, length);
      if(retval == -1){
        throw new RuntimeException("munmap() failed (errno=" + (int)hndErrno.get(errnoSeg, 0L) + ")");
      }
      return retval; // it should be 0
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

}
