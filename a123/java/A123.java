import java.lang.reflect.*;

import jdk.vm.ci.runtime.*;
import jdk.vm.ci.meta.*;
import jdk.vm.ci.code.*;
import jdk.vm.ci.hotspot.*;
import jdk.vm.ci.amd64.*;

import jdk.vm.ci.code.test.*;
import jdk.vm.ci.code.test.amd64.AMD64TestAssembler;


public class A123{

  private static void prepare() throws Exception{
    JVMCIBackend backend = JVMCI.getRuntime().getHostJVMCIBackend();
    MetaAccessProvider metaAccess = backend.getMetaAccess();
    CodeCacheProvider codeCache = backend.getCodeCache();
    TestHotSpotVMConfig config = new TestHotSpotVMConfig(
                           HotSpotJVMCIRuntime.runtime().getConfigStore());
    AMD64TestAssembler asm = new AMD64TestAssembler(codeCache, config);

    HotSpotResolvedJavaMethod resolvedMethod =
                (HotSpotResolvedJavaMethod)metaAccess.lookupJavaMethod(
                                       A123.class.getMethod("comp", int.class));
    asm.emitPrologue();
    asm.emitLoadInt(AMD64.rax, 1);
    asm.emitIntRet(AMD64.rax);
    asm.emitEpilogue();

    HotSpotCompiledNmethod nm =
                    (HotSpotCompiledNmethod)asm.finish(resolvedMethod);
    Field bciField = HotSpotCompiledNmethod.class.getDeclaredField("entryBCI");
    bciField.setAccessible(true);
    bciField.setInt(nm, -1);
    codeCache.setDefaultCode(resolvedMethod, nm);
  }

  public static boolean comp(int a){
    return (a == 1) && (a == 2) && (a == 3);
  }

  public static void main(String[] args) throws Exception{
    prepare();
    System.out.println(comp(args.length));
  }

}
