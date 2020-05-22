import org.graalvm.polyglot.*;


public class GetPID{
  public static void main(String[] args) throws Exception{
    try(var ctx = Context.newBuilder("nfiwrapper")
                         .allowNativeAccess(true)
                         .build()){
      var getpid = ctx.eval("nfiwrapper", "load 'libc.so.6'")
                      .getMember("getpid")
                      .invokeMember("bind", "():SINT32");

      int pid = getpid.execute().asInt();
      System.out.println("PID: " + pid);
    }
  }
}
