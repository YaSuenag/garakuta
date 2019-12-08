import org.graalvm.polyglot.*;
import java.io.*;

public class Nabeatsu{

  public static void main(String[] args) throws Exception{
    int max = 0;
    if((args.length != 1) ||
       ((max = Integer.parseInt(args[0])) <= 0)){
      System.err.println("baka");
      System.exit(3);
    }

    Source src = Source.newBuilder("llvm", new File("../native/lib/libnabeatsu.so"))
                       .build();
    try(Context ctx = Context.newBuilder("llvm")
                             .allowNativeAccess(true)
                             .build()){
      ctx.eval(src);
      Value is_aho = ctx.getBindings("llvm")
                        .getMember("is_aho");

      for(int i = 1; i <= max; i++){
        System.out.print(i);
        if(is_aho.execute(i).asInt() == 1){
          System.out.print(" [aho]");
        }
        System.out.println();
      }

    }

  }

}
