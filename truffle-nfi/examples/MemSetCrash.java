import org.graalvm.polyglot.*;


public class MemSetCrash{
  public static void main(String[] args) throws Exception{
    try(var ctx = Context.newBuilder("nfiwrapper")
                         .allowNativeAccess(true)
                         .build()){
      var memset = ctx.eval("nfiwrapper", "load 'libc.so.6'")
                      .getMember("memset")
                      .invokeMember("bind", "(POINTER, SINT32, UINT32):POINTER");
      memset.execute(null, 0, 100); // memset(NULL, 0, 100)
    }
  }
}
