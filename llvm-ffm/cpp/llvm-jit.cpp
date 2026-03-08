#include <jni.h>

#include <memory>
#include <string>
#include <format>
#include <chrono>

#include <clang/CodeGen/CodeGenAction.h>
#include <clang/Frontend/CompilerInstance.h>
#include <clang/Frontend/CompilerInvocation.h>
#include <clang/Frontend/Utils.h>
#include <clang/Basic/TargetInfo.h>
#include <clang/Frontend/TextDiagnosticPrinter.h>
#include <clang/Basic/TargetOptions.h>
#include <clang/Lex/PreprocessorOptions.h>

#include <llvm/ExecutionEngine/Orc/LLJIT.h>
#include <llvm/IR/Module.h>
#include <llvm/ADT/IntrusiveRefCntPtr.h>
#include <llvm/Support/TargetSelect.h>
#include <llvm/TargetParser/Host.h>
#include <llvm/TargetParser/Triple.h> 


static jfieldID ptrJIT_ID = 0;
static jclass runtimeExCls = nullptr;
static const char* sys_incl_paths[] = {SYSTEM_INCLUDE_PATHS};


extern "C" JNIEXPORT void JNICALL Java_LLVMJit_init0(JNIEnv *env, jclass cls){
  ptrJIT_ID = env->GetFieldID(cls, "ptrJIT", "J");
  if(env->ExceptionCheck()){
    return;
  }

  jclass c = env->FindClass("java/lang/RuntimeException"); // Should not fail.
  runtimeExCls = static_cast<jclass>(env->NewGlobalRef(c));

  llvm::InitializeNativeTarget();
  llvm::InitializeNativeTargetAsmPrinter();
  llvm::InitializeNativeTargetAsmParser();
}

extern "C" JNIEXPORT void Java_LLVMJit_compile0(JNIEnv *env, jobject obj, jstring src, jint optoLevel){
  // Generate dummy source file name
  auto tid = gettid();
  auto timestamp = std::chrono::duration_cast<std::chrono::microseconds>(
    std::chrono::system_clock::now().time_since_epoch()
  ).count();
  std::string dummy_srcname = std::format("{}-{}.c", tid, timestamp);

  // Prepare error string
  std::string error_log;
  llvm::raw_string_ostream error_stream(error_log);

  // Configure CompilerInstance
  clang::CompilerInstance ci;
  auto diag_opts_ref = ci.getInvocation().getDiagnosticOpts();
  auto diag_client = new clang::TextDiagnosticPrinter(error_stream, diag_opts_ref);
  clang::IntrusiveRefCntPtr<clang::DiagnosticIDs> diag_ids = new clang::DiagnosticIDs();
  clang::IntrusiveRefCntPtr<clang::DiagnosticsEngine> diags = new clang::DiagnosticsEngine(diag_ids, diag_opts_ref, diag_client);
  if(!clang::CompilerInvocation::CreateFromArgs(ci.getInvocation(), {dummy_srcname.c_str()}, *diags)){
    env->ThrowNew(runtimeExCls, "CreateFromArgs() failed.");
    return;
  }
  ci.setDiagnostics(diags.get());
  ci.getCodeGenOpts().OptimizationLevel = optoLevel;

  // Configure SourceManager
  ci.createFileManager();
  ci.createSourceManager(ci.getFileManager());

  // Configure TargetInfo
  ci.setTarget(clang::TargetInfo::CreateTargetInfo(ci.getDiagnostics(), ci.getInvocation().getTargetOpts()));

  // Load given source
  const char *char_src = env->GetStringUTFChars(src, nullptr);
  std::string str_src = char_src;
  env->ReleaseStringUTFChars(src, char_src);
  std::unique_ptr<llvm::MemoryBuffer> buffer = llvm::MemoryBuffer::getMemBuffer(str_src.c_str(), dummy_srcname.c_str());
  ci.getInvocation().getPreprocessorOpts().addRemappedFile(dummy_srcname.c_str(), buffer.release());

  // Configure include paths
  ci.getInvocation().getTargetOpts().Triple = llvm::sys::getDefaultTargetTriple();
  auto &headerSearchOpts = ci.getHeaderSearchOpts();
  headerSearchOpts.UseBuiltinIncludes = 1;
  headerSearchOpts.UseStandardSystemIncludes = 1;
  for(int i = 0; i < sizeof(sys_incl_paths) / sizeof(const char*); i++){
    headerSearchOpts.AddPath(sys_incl_paths[i], clang::frontend::System, false, false);
  }

  // Execute action
  auto ctx = std::make_unique<llvm::LLVMContext>();
  clang::EmitLLVMOnlyAction action(ctx.get());
  if(!ci.ExecuteAction(action)){
    error_stream.flush();
    env->ThrowNew(runtimeExCls, error_log.c_str());
    return;
  }
  std::unique_ptr<llvm::Module> mod = action.takeModule();
  if(!mod){
    env->ThrowNew(runtimeExCls, "module is NULL.");
    return;
  }

  // Configure JIT compiler
  auto JTMB = llvm::orc::JITTargetMachineBuilder::detectHost();
  if(!JTMB){
    std::string msg = llvm::toString(JTMB.takeError());
    env->ThrowNew(runtimeExCls, msg.c_str());
    return;
  }
  JTMB->setCodeGenOptLevel(static_cast<llvm::CodeGenOptLevel>(optoLevel));
  auto jit_expected = llvm::orc::LLJITBuilder()
                        .setJITTargetMachineBuilder(std::move(*JTMB))
                        .create();
  if(!jit_expected){
    std::string msg = llvm::toString(jit_expected.takeError());
    env->ThrowNew(runtimeExCls, msg.c_str());
    return;
  }
  auto jit = std::move(*jit_expected);

  // Compile
  if(auto err = jit->addIRModule(llvm::orc::ThreadSafeModule(std::move(mod), std::move(ctx)))){
    std::string msg = llvm::toString(std::move(err));
    env->ThrowNew(runtimeExCls, msg.c_str());
    return;
  }

  env->SetLongField(obj, ptrJIT_ID, reinterpret_cast<jlong>(jit.release()));
}

extern "C" JNIEXPORT jlong Java_LLVMJit_getFunctionPointer0(JNIEnv *env, jobject obj, jstring name){
  llvm::orc::LLJIT* jit = reinterpret_cast<llvm::orc::LLJIT*>(env->GetLongField(obj, ptrJIT_ID));
  if(env->ExceptionCheck()){
    return -1;
  }

  const char *char_name = env->GetStringUTFChars(name, nullptr);
  auto symbol_expected = jit->lookup(char_name);
  env->ReleaseStringUTFChars(name, char_name);
  if(!symbol_expected){
    std::string msg = llvm::toString(symbol_expected.takeError());
    env->ThrowNew(runtimeExCls, msg.c_str());
    return -1;
  }
  return symbol_expected->getValue();
}

extern "C" JNIEXPORT void Java_LLVMJit_dispose0(JNIEnv *env, jobject obj){
  llvm::orc::LLJIT* jit = reinterpret_cast<llvm::orc::LLJIT*>(env->GetLongField(obj, ptrJIT_ID));
  if(jit != nullptr){
    delete jit;
  }
  env->DeleteGlobalRef(runtimeExCls);
}
