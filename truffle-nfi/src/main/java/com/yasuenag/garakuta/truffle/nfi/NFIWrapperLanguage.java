package com.yasuenag.garakuta.truffle.nfi;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;


@TruffleLanguage.Registration(
  id = "nfiwrapper", name = "TruffleNFIWrapper", version = "0.1.0",
  characterMimeTypes = NFIWrapperLanguage.MIME_TYPE,
  dependentLanguages = {"nfi"},
  contextPolicy = TruffleLanguage.ContextPolicy.SHARED)
public class NFIWrapperLanguage extends TruffleLanguage<NFIWrapperContext>{

  public static final String MIME_TYPE = "application/x-native-wrapper";

  @Override
  protected NFIWrapperContext createContext(TruffleLanguage.Env env){
    return new NFIWrapperContext(env);
  }

  @Override
  protected boolean patchContext(NFIWrapperContext context, TruffleLanguage.Env newEnv){
    context.setEnv(newEnv);
    return true;
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception{
    var nfiSource = request.getSource().getCharacters();
    var env = getCurrentContext(this.getClass()).getEnv();
    return env.parseInternal(Source.newBuilder("nfi", nfiSource, null).build());
  }

  @Override
  protected boolean isThreadAccessAllowed(Thread thread, boolean singleThreaded){
    return true;
  }

}
