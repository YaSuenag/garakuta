import java.lang.foreign.*;


public class SegvInGettimeofdayWithFFM{

  public static void main(String[] args) throws Throwable{
    // define arguments as long to pass invalid pointer
    var desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG);
    var linker = Linker.nativeLinker();
    var gettimeofdayPtr = linker.defaultLookup().findOrThrow("gettimeofday");
    var gettimeofday = linker.downcallHandle(gettimeofdayPtr, desc);

    gettimeofday.invoke(100L, 200L);
  }

}
