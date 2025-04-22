#define CL_HPP_ENABLE_EXCEPTIONS
#define CL_HPP_TARGET_OPENCL_VERSION 300

#include <iostream>
#include <fstream>
#include <vector>

#include <CL/opencl.hpp>

#include "../common.h"


class CLUtil{

  private:
    cl::Program program;

  public:
    void setup_cl_platform();
    void setup_cl_program(const char *fname);
    void exec_kernel(const char *kernel_name, std::vector<int> &buf);
};

void CLUtil::setup_cl_platform(){
  std::vector<cl::Platform> platforms;
  cl::Platform::get(&platforms);

  if(platforms.size() == 0){
    std::cerr << "Could not get platform." << std::endl;
    _Exit(3);
  }

  bool found = false;
  for(auto &p : platforms){
    if(p.getInfo<CL_PLATFORM_VERSION>().find("OpenCL 3.") != std::string::npos){
      std::string platform_name = p.getInfo<CL_PLATFORM_NAME>();
      std::cout << p.getInfo<CL_PLATFORM_NAME>() 
                << " (" << p.getInfo<CL_PLATFORM_VERSION>() << ")"
                << std::endl;

      if(cl::Platform::setDefault(p) != p){
        std::cerr << "Could not set \"" << platform_name << "\" as default."
                  << std::endl;
        _Exit(3);
      }

      found = true;
      break;
    }
  }

  if(!found){
    std::cerr << "Could not find OpenCL 3.0 platform." << std::endl;
    _Exit(3);
  }

}

void CLUtil::setup_cl_program(const char *fname){
  std::ifstream f(fname);
  if(!f.is_open()){
    std::cerr << "Could not open " << fname << std::endl;
    _Exit(3);
  }
  std::string cl_src = std::string(std::istreambuf_iterator<char>(f),
                                   std::istreambuf_iterator<char>());
  cl::Program prog(cl_src);
  try{
    prog.build("-cl-std=CL3.0");
  }
  catch(cl::BuildError err){
    std::cerr << "Could not build CL source!" << std::endl;

    cl_int build_error = CL_SUCCESS;
    auto build_info = prog.getBuildInfo<CL_PROGRAM_BUILD_LOG>(&build_error);
    for(auto &p : build_info){
      std::cerr << p.second << std::endl;
    }

    _Exit(3);
  }

  program = prog;
}

void CLUtil::exec_kernel(const char *kernel_name, std::vector<int> &buf){
  auto kernel = cl::KernelFunctor<cl::Buffer>(program, kernel_name);
  cl::Buffer out(begin(buf), end(buf), false);

  kernel(cl::EnqueueArgs(cl::NDRange(buf.size(), 1, 1),
                         cl::NDRange(buf.size() / 10)), out);

  cl::copy(out, begin(buf), end(buf));
}

int main(int argc, char *argv[]){
  int max = check_arg(argc, argv);

  CLUtil util;

  util.setup_cl_platform();
  util.setup_cl_program("nabeatsu.cl");

  std::vector<int> vals(max);
  util.exec_kernel("nabeatsu", vals);

  for(int idx = 0; idx < vals.size(); idx++){
    std::cout << (idx + 1);

    if(vals[idx] == 3){
      std::cout << " [aho]";
    }

    std::cout << std::endl;
  }

  return 0;
}

