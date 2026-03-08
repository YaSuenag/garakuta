import java.lang.foreign.*;
import java.lang.invoke.*;


public class Test{

  private static final String C_SRC = """
    #define _GNU_SOURCE
    #include <stdio.h>
    #include <unistd.h>

    int hello(char *name){
      return printf("Hello %s\\n", name);
    }

    int tid(){
      return gettid();
    }
                                      """;

  private static final String MODULE_NAME = "llvm-test";

  public static void main(String[] args) throws Throwable{
    try(var llvm = new LLVMJit()){
      var bc = llvm.generateBitCode(C_SRC);
      llvm.compile(bc, MODULE_NAME, 3);

      var hello = llvm.getFunction(
        "hello",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
      );
      var tid = llvm.getFunction(
        "tid",
        FunctionDescriptor.of(ValueLayout.JAVA_INT)
      );

      try(var arena = Arena.ofConfined()){
        hello.invoke(arena.allocateFrom("test"));
      }
      IO.println("TID = " + (int)tid.invoke());
    }
  }

}
