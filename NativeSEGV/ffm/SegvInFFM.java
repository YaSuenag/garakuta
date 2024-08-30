import static java.lang.foreign.MemorySegment.*;
import static java.lang.foreign.ValueLayout.*;


public class SegvInFFM{

  public static void main(String[] args){
    var nullMem = NULL.reinterpret(JAVA_INT.byteSize());
    nullMem.set(JAVA_INT, 0, 1);
  }

}
